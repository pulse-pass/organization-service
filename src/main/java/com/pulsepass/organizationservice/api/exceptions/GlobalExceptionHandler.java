package com.pulsepass.organizationservice.api.exceptions;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<Class<? extends DomainException>, HttpStatus> STATUS_MAP = Map.of(
            OrganizationAlreadyExistsException.class, HttpStatus.CONFLICT,
            UnauthorizedInternalRequestException.class, HttpStatus.UNAUTHORIZED
    );

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String messageKey,
            String code,
            List<FieldErrorResponse> extraFields
    ) {
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status.getReasonPhrase(), message, code, extraFields));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        logger.warn(
                "GlobalExceptionHandler.handleDomain - Domain exception {}",
                Map.of("code", ex.getCode(), "message", ex.getMessage())
        );
        HttpStatus status = STATUS_MAP.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        return build(status, ex.getMessageKey(), ex.getCode(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldErrorResponse::fromFieldError)
                .toList();
        List<FieldErrorResponse> extraFields = fieldErrors.isEmpty()
                ? List.of(new FieldErrorResponse("body", "INVALID_FORMAT"))
                : fieldErrors;
        logger.warn(
                "GlobalExceptionHandler.handleInvalidRequest - Invalid request fields {}",
                Map.of("extra_fields", extraFields)
        );
        return build(HttpStatus.BAD_REQUEST, "error.bad_request", "GEN00400", extraFields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException ex) {
        List<FieldErrorResponse> extraFields = List.of(new FieldErrorResponse("body", "INVALID_FORMAT"));
        logger.warn(
                "GlobalExceptionHandler.handleUnreadableRequest - Request body could not be read {}",
                Map.of("extra_fields", extraFields)
        );
        return build(HttpStatus.BAD_REQUEST, "error.bad_request", "GEN00400", extraFields);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
        logger.warn(
                "GlobalExceptionHandler.handleNotFound - Resource not found {}",
                Map.of("path", ex.getResourcePath())
        );
        return build(HttpStatus.NOT_FOUND, "error.not_found", "GEN00404", List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex
    ) {
        logger.warn(
                "GlobalExceptionHandler.handleMethodNotAllowed - Method not allowed {}",
                Map.of("method", ex.getMethod())
        );
        return build(HttpStatus.METHOD_NOT_ALLOWED, "error.method_not_allowed", "GEN00405", List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex
    ) {
        logger.warn(
                "GlobalExceptionHandler.handleUnsupportedMediaType - Unsupported media type {}",
                Map.of("content_type", String.valueOf(ex.getContentType()))
        );
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "error.unsupported_media_type", "GEN00415", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        logger.error("GlobalExceptionHandler.handleGeneric - Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal", "GEN00500", List.of());
    }
}
