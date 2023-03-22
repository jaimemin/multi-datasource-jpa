package com.tistory.jaimemin.multidatasourcejpa.multitenancy.config.tenant.hibernate;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.config.TenantContextHolder;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("currentTenantIdentifierResolver")
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    private static final String DEFAULT_TENANT = "default";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContextHolder.getTenantId();

        return ObjectUtils.isEmpty(tenantId) ? DEFAULT_TENANT : tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
