package com.pulsepass.organizationservice.api.validators;

import com.pulsepass.organizationservice.domain.exceptions.MissingSelectedAgentKeyException;
import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import java.util.Arrays;
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

    private boolean hasAdminRole(String roles) {
        if (roles == null) {
            return false;
        }
        return Arrays.stream(roles.split(",")).map(String::trim).anyMatch("admin"::equals);
    }

    public void validateInternalTokenOrAdmin(String token, String roles) {
        if (internalToken.equals(token)) {
            return;
        }
        if (hasAdminRole(roles)) {
            return;
        }
        logger.warn("InternalRequestValidator.validateInternalTokenOrAdmin - Missing or invalid internal token");
        throw new UnauthorizedInternalRequestException();
    }

    public void validateSelectedAgentKey(String selectedAgentKey) {
        if (selectedAgentKey == null || selectedAgentKey.isBlank()) {
            logger.warn("InternalRequestValidator.validateSelectedAgentKey - Missing selected agent key");
            throw new MissingSelectedAgentKeyException();
        }
    }
}
