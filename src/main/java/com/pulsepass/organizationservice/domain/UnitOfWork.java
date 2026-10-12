package com.pulsepass.organizationservice.domain;

public interface UnitOfWork {

    void begin();

    void commit();

    void rollback();

    boolean isActive();
}
