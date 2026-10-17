package com.pulsepass.organizationservice.unit.api.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.pulsepass.organizationservice.api.exceptions.ErrorResponse;
import com.pulsepass.organizationservice.api.exceptions.ExtraFieldErrorResponse;
import com.pulsepass.organizationservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("error.internal", Locale.ENGLISH, "internal");
        messages.addMessage("error.bad_request", Locale.ENGLISH, "bad request");
        messages.addMessage("error.organization.already_exists", Locale.ENGLISH, "already exists");
        messages.addMessage("error.unauthorized", Locale.ENGLISH, "unauthorized");
        messages.addMessage("error.not_found", Locale.ENGLISH, "not found");
        messages.addMessage("error.method_not_allowed", Locale.ENGLISH, "method not allowed");
        messages.addMessage("error.unsupported_media_type", Locale.ENGLISH, "unsupported media type");
        handler = new GlobalExceptionHandler(messages);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }

    @Test
    void handleDomain_alreadyExists_returns409WithOrg00001() {
        var response = handler.handleDomain(new OrganizationAlreadyExistsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "Conflict",
                "already exists",
                "ORG00001",
                List.of()
        ));
    }

    @Test
    void handleDomain_unauthorizedInternalRequest_returns401WithGen00401() {
        var response = handler.handleDomain(new UnauthorizedInternalRequestException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "Unauthorized",
                "unauthorized",
                "GEN00401",
                List.of()
        ));
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

        var response = handler.handleDomain(unmapped);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("ORG99999");
    }

    @Test
    void handleInvalidRequest_methodArgumentNotValid_returns400WithFields() throws Exception {
        var parameter = new MethodParameter(getClass().getDeclaredMethod("dummy", String.class), 0);
        var bindingResult = new BeanPropertyBindingResult(this, "target");
        bindingResult.addError(new FieldError(
                "target", "phoneNumber.areaCode", null, false, new String[]{"Pattern"}, null, "invalid"
        ));
        var exception = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleInvalidRequest(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "Bad Request",
                "bad request",
                "GEN00400",
                List.of(new ExtraFieldErrorResponse("phone_number.area_code", "INVALID_FORMAT"))
        ));
    }

    @Test
    void handleInvalidRequest_noFieldErrors_returns400WithBodyField() throws Exception {
        var parameter = new MethodParameter(getClass().getDeclaredMethod("dummy", String.class), 0);
        var exception = new MethodArgumentNotValidException(parameter, new BeanPropertyBindingResult(this, "target"));

        var response = handler.handleInvalidRequest(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "Bad Request",
                "bad request",
                "GEN00400",
                List.of(new ExtraFieldErrorResponse("body", "INVALID_FORMAT"))
        ));
    }

    @Test
    void handleUnreadableRequest_messageNotReadable_returns400WithBodyField() {
        var exception = new HttpMessageNotReadableException("malformed", mock(HttpInputMessage.class));

        var response = handler.handleUnreadableRequest(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "Bad Request",
                "bad request",
                "GEN00400",
                List.of(new ExtraFieldErrorResponse("body", "INVALID_FORMAT"))
        ));
    }

    @Test
    void handleGeneric_anyException_returns500WithGen00500() {
        var response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .isEqualTo(new ErrorResponse("Internal Server Error", "internal", "GEN00500", List.of()));
    }

    @Test
    void handleNotFound_noResourceFound_returns404WithGen00404() {
        var exception = new NoResourceFoundException(HttpMethod.GET, "/missing", "missing");

        var response = handler.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Not Found", "not found", "GEN00404", List.of()));
    }

    @Test
    void handleMethodNotAllowed_methodNotSupported_returns405WithGen00405() {
        var exception = new HttpRequestMethodNotSupportedException("DELETE");

        var response = handler.handleMethodNotAllowed(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody())
                .isEqualTo(new ErrorResponse("Method Not Allowed", "method not allowed", "GEN00405", List.of()));
    }

    @Test
    void handleUnsupportedMediaType_mediaTypeNotSupported_returns415WithGen00415() {
        var exception = new HttpMediaTypeNotSupportedException(
                MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON));

        var response = handler.handleUnsupportedMediaType(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isEqualTo(
                new ErrorResponse("Unsupported Media Type", "unsupported media type", "GEN00415", List.of()));
    }
}
