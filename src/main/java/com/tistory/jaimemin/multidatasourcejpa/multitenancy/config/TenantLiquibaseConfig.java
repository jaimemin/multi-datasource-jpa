package com.tistory.jaimemin.multidatasourcejpa.multitenancy.config;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.config.tenant.liquidbase.DynamicDataSourceBasedMultiTenantSpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy(false)
@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
@ConditionalOnProperty(name = "multitenancy.tenant.liquibase.enabled", havingValue = "true", matchIfMissing = true)
public class TenantLiquibaseConfig {

    @Bean
    @ConfigurationProperties("multitenancy.tenant.liquibase")
    public LiquibaseProperties tenantLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public DynamicDataSourceBasedMultiTenantSpringLiquibase tenantLiquibase() {
        return new DynamicDataSourceBasedMultiTenantSpringLiquibase();
    }
}
