package com.pulsepass.organizationservice.infrastructure.jpa;

import com.pulsepass.organizationservice.domain.UnitOfWork;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class JpaUnitOfWork implements UnitOfWork {

    private final PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;

    public JpaUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void begin() {
        if (isActive()) {
            return;
        }
        var definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.transactionStatus = transactionManager.getTransaction(definition);
    }

    @Override
    public void commit() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction to commit");
        }
        var status = transactionStatus;
        transactionStatus = null;
        transactionManager.commit(status);
    }

    @Override
    public void rollback() {
        if (!isActive()) {
            throw new IllegalStateException("No active transaction to rollback");
        }
        var status = transactionStatus;
        transactionStatus = null;
        transactionManager.rollback(status);
    }

    @Override
    public boolean isActive() {
        return transactionStatus != null && !transactionStatus.isCompleted();
    }

    @PreDestroy
    public void cleanup() {
        if (isActive()) {
            rollback();
        }
    }
}
