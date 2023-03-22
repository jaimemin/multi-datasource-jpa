package com.tistory.jaimemin.multidatasourcejpa.multitenancy.config;

import lombok.RequiredArgsConstructor;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JpaProperties.class)
@EnableJpaRepositories(
        basePackages = { "${multitenancy.tenant.repository.packages}" },
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
public class TenantPersistenceConfig {

    private final ConfigurableListableBeanFactory beanFactory;

    private final JpaProperties jpaProperties;

    @Value("${multitenancy.tenant.entityManager.packages}")
    private String entityPackages;

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            @Qualifier("dynamicDataSourceBasedMultiTenantConnectionProvider") MultiTenantConnectionProvider connectionProvider,
            @Qualifier("currentTenantIdentifierResolver") CurrentTenantIdentifierResolver tenantResolver) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setPersistenceUnitName("tenant-persistence-unit");
        entityManagerFactoryBean.setPackagesToScan(entityPackages);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setJpaPropertyMap(getProperties(connectionProvider, tenantResolver));

        return entityManagerFactoryBean;
    }

    @Bean
    @Primary
    public JpaTransactionManager tenantTransactionManager(@Qualifier("tenantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager tenantTransactionManager = new JpaTransactionManager();
        tenantTransactionManager.setEntityManagerFactory(entityManagerFactory);

        return tenantTransactionManager;
    }

    private Map<String, Object> getProperties(MultiTenantConnectionProvider connectionProvider, CurrentTenantIdentifierResolver tenantResolver) {
        Map<String, Object> properties = new HashMap<>(this.jpaProperties.getProperties());
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
        properties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        properties.put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(this.beanFactory));
        properties.put(AvailableSettings.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);

        return properties;
    }
}
