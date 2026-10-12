package com.pulsepass.organizationservice.unit.api.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.pulsepass.organizationservice.api.exceptions.ErrorResponse;
import com.pulsepass.organizationservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("error.internal", Locale.ENGLISH, "internal");
        messages.addMessage("error.bad_request", Locale.ENGLISH, "bad request");
        messages.addMessage("error.organization.already_exists", Locale.ENGLISH, "already exists");
        messages.addMessage("error.unauthorized", Locale.ENGLISH, "unauthorized");
        handler = new GlobalExceptionHandler(messages);
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }

    @Test
    void handleDomain_alreadyExists_returns409WithOrg00001() {
        var response = handler.handleDomain(new OrganizationAlreadyExistsException(), Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Conflict", "already exists", "ORG00001"));
    }

    @Test
    void handleDomain_unauthorizedInternalRequest_returns401WithGen00401() {
        var response = handler.handleDomain(new UnauthorizedInternalRequestException(), Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Unauthorized", "unauthorized", "GEN00401"));
    }

    @Test
    void handleDomain_unmappedDomainException_defaultsTo500() {
        DomainException unmapped = new DomainException("unmapped") {
            @Override
            public String getCode() {
                return "ORG99999";
            }

            @Override
            public String getMessageKey() {
                return "error.internal";
            }
        };

        var response = handler.handleDomain(unmapped, Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("ORG99999");
    }

    @Test
    void handleBadRequest_methodArgumentNotValid_returns400WithGen00400() throws Exception {
        var parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0);
        var exception = new MethodArgumentNotValidException(parameter, new BeanPropertyBindingResult(this, "target"));

        var response = handler.handleBadRequest(exception, Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Bad Request", "bad request", "GEN00400"));
    }

    @Test
    void handleBadRequest_messageNotReadable_returns400WithGen00400() {
        var exception = new HttpMessageNotReadableException("malformed", mock(HttpInputMessage.class));

        var response = handler.handleBadRequest(exception, Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("GEN00400");
    }

    @Test
    void handleGeneric_anyException_returns500WithGen00500() {
        var response = handler.handleGeneric(new RuntimeException("boom"), Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .isEqualTo(new ErrorResponse("Internal Server Error", "internal", "GEN00500"));
    }
}
