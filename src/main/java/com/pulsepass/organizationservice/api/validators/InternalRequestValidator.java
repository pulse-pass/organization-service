package com.pulsepass.organizationservice.api.validators;

import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InternalRequestValidator {

    private static final Logger logger = LoggerFactory.getLogger(InternalRequestValidator.class);

    private final String internalToken;

    public InternalRequestValidator(@Value("${app.internal-token}") String internalToken) {
        this.internalToken = internalToken;
    }

    private boolean isValid(String token) {
        return internalToken.equals(token);
    }

    public void validateInternalRequest(String token) {
        if (!isValid(token)) {
            logger.warn("InternalRequestValidator.validateInternalRequest - Missing or invalid internal token");
            throw new UnauthorizedInternalRequestException();
        }
    }
}
