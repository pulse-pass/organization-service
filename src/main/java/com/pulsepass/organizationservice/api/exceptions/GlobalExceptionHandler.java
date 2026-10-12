package com.pulsepass.organizationservice.api.exceptions;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String messageKey, String code, Locale locale) {
        String message = messageSource.getMessage(messageKey, null, locale);
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status.getReasonPhrase(), message, code));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex, Locale locale) {
        logger.warn("GlobalExceptionHandler.handleDomain - {}: {}", ex.getCode(), ex.getMessage());
        HttpStatus status = STATUS_MAP.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        return build(status, ex.getMessageKey(), ex.getCode(), locale);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, Locale locale) {
        logger.warn("GlobalExceptionHandler.handleBadRequest - Invalid request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "error.bad_request", "GEN00400", locale);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, Locale locale) {
        logger.error("GlobalExceptionHandler.handleGeneric - Unexpected error: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal", "GEN00500", locale);
    }
}
