package com.tistory.jaimemin.multidatasourcejpa.multitenancy.service;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.dto.DataSourceDto;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity.DataSourceEntity;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.repository.DataSourceRepository;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(LiquibaseProperties.class)
public class DataSourceService {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final ResourceLoader resourceLoader;

    private final DataSourceRepository repository;

    @Qualifier("tenantLiquibaseProperties")
    private final LiquibaseProperties liquibaseProperties;

    public DataSourceEntity findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("테넌트 " + tenantId + "가 존재하지 않습니다."));
    }

    public void createDataSource(DataSourceDto dataSourceDto) {
        try {
            createDatabase(dataSourceDto.getDbName(), dataSourceDto.getPassword());
        } catch (DataAccessException e) {
            throw new RuntimeException(dataSourceDto.getDbName() + " DB를 생성하는 도중 에러가 발생했습니다.");
        }

        try (Connection connection = DriverManager.getConnection(dataSourceDto.getUrl(), dataSourceDto.getDbName(), dataSourceDto.getPassword())) {
            DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
            runLiquibase(tenantDataSource);
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException(dataSourceDto.getDbName() + " DB를 생성하는 도중 에러가 발생했습니다.");
        }

        repository.save(DataSourceEntity.builder()
                .tenantId(dataSourceDto.getTenantId())
                .url(dataSourceDto.getUrl())
                .dbName(dataSourceDto.getDbName())
                .password(dataSourceDto.getPassword())
                .build());
    }

    private void createDatabase(String dbName, String password) {
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE DATABASE " + dbName));
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE USER " + dbName + " WITH PASSWORD '" + password + "'"));
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + dbName));
    }

    private void runLiquibase(DataSource dataSource) throws LiquibaseException {
        SpringLiquibase liquibase = getSpringLiquibase(dataSource);
        liquibase.afterPropertiesSet();
    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        liquibase.setLabels(liquibaseProperties.getLabels());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());

        return liquibase;
    }
}
