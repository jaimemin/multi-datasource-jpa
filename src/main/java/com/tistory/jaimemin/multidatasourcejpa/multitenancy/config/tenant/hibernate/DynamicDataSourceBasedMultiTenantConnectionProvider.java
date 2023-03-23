package com.tistory.jaimemin.multidatasourcejpa.multitenancy.config.tenant.hibernate;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity.DataSourceManagement;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.repository.DataSourceManagementRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DynamicDataSourceBasedMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    @Autowired
    @Qualifier("masterDataSource")
    private DataSource masterDataSource;

    @Autowired
    @Qualifier("masterDataSourceProperties")
    private DataSourceProperties dataSourceProperties;

    @Autowired
    private DataSourceManagementRepository dataSourceManagementRepository;

    @Value("${multitenancy.datasource-cache.maximumSize:100}")
    private Long maximumSize;

    @Value("${multitenancy.datasource-cache.expireAfterAccess:10}")
    private Integer expireAfterAccess;

    private LoadingCache<String, DataSource> tenantDataSources;

    @PostConstruct
    private void createCache() {
        tenantDataSources = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess, TimeUnit.MINUTES)
                .removalListener((RemovalListener<String, DataSource>) (tenantId, dataSource, removalCause) -> {
                    HikariDataSource ds = (HikariDataSource) dataSource;
                    ds.close(); // tear down properly
                })
                .build(tenantId -> {
                            DataSourceManagement dataSourceManagement = dataSourceManagementRepository.findByTenantId(tenantId)
                                    .orElseThrow(() -> new RuntimeException("해당 테넌트가 존재하지 않습니다: " + tenantId));

                            return createAndConfigureDataSource(dataSourceManagement);
                        }
                );
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return masterDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantId) {
        try {
            return tenantDataSources.get(tenantId);
        } catch (Exception e) {
            throw new RuntimeException("테넌트 " + tenantId + "의 DataSource를 가지고 오는데 실패했습니다.");
        }
    }

    private DataSource createAndConfigureDataSource(DataSourceManagement dataSource) {
        HikariDataSource ds = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .driverClassName(dataSource.getDriverClassName())
                .build();
        ds.setUsername(dataSource.getUsername());
        ds.setPassword(dataSource.getPassword());
        ds.setJdbcUrl(dataSource.getUrl() + dataSource.getDbName());

        return ds;
    }

}
