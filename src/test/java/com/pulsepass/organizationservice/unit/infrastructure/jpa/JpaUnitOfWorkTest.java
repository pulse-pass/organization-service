package com.pulsepass.organizationservice.unit.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.infrastructure.jpa.JpaUnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

class JpaUnitOfWorkTest {

    private PlatformTransactionManager transactionManager;
    private TransactionStatus status;
    private JpaUnitOfWork unitOfWork;

    @BeforeEach
    void setUp() {
        transactionManager = mock(PlatformTransactionManager.class);
        status = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        when(status.isCompleted()).thenReturn(false);
        unitOfWork = new JpaUnitOfWork(transactionManager);
    }

    @Test
    void isActive_beforeBegin_returnsFalse() {
        assertThat(unitOfWork.isActive()).isFalse();
    }

    @Test
    void begin_noTransaction_opensRequiredTransactionAndBecomesActive() {
        unitOfWork.begin();

        assertThat(unitOfWork.isActive()).isTrue();
        var captor = org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager).getTransaction(captor.capture());
        assertThat(captor.getValue().getPropagationBehavior())
                .isEqualTo(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @Test
    void begin_alreadyActive_doesNotOpenSecondTransaction() {
        unitOfWork.begin();
        unitOfWork.begin();

        verify(transactionManager, times(1)).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    void commit_activeTransaction_commitsAndBecomesInactive() {
        unitOfWork.begin();

        unitOfWork.commit();

        verify(transactionManager).commit(status);
        assertThat(unitOfWork.isActive()).isFalse();
    }

    @Test
    void commit_noTransaction_throwsIllegalState() {
        assertThatThrownBy(() -> unitOfWork.commit())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("commit");

        verify(transactionManager, never()).commit(any());
    }

    @Test
    void rollback_activeTransaction_rollsBackAndBecomesInactive() {
        unitOfWork.begin();

        unitOfWork.rollback();

        verify(transactionManager).rollback(status);
        assertThat(unitOfWork.isActive()).isFalse();
    }

    @Test
    void rollback_noTransaction_throwsIllegalState() {
        assertThatThrownBy(() -> unitOfWork.rollback())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rollback");

        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void isActive_completedTransaction_returnsFalse() {
        unitOfWork.begin();
        when(status.isCompleted()).thenReturn(true);

        assertThat(unitOfWork.isActive()).isFalse();
    }

    @Test
    void cleanup_activeTransaction_rollsBack() {
        unitOfWork.begin();

        unitOfWork.cleanup();

        verify(transactionManager).rollback(status);
    }

    @Test
    void cleanup_noTransaction_doesNothing() {
        unitOfWork.cleanup();

        verify(transactionManager, never()).rollback(any());
    }
}
