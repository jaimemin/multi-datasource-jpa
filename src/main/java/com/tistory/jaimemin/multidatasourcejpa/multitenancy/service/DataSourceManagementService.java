package com.tistory.jaimemin.multidatasourcejpa.multitenancy.service;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.dto.DataSourceManagementDto;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity.DataSourceManagement;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.repository.DataSourceManagementRepository;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.repackaged.org.apache.commons.collections4.functors.ExceptionClosure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(LiquibaseProperties.class)
public class DataSourceManagementService {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final ResourceLoader resourceLoader;

    private final DataSourceManagementRepository repository;

    @Qualifier("tenantLiquibaseProperties")
    private final LiquibaseProperties liquibaseProperties;

    public List<DataSourceManagement> findALl() {
        return repository.findAll();
    }

    public DataSourceManagement findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("테넌트 " + tenantId + "가 존재하지 않습니다."));
    }

    public void createDataSource(DataSourceManagementDto dataSourceManagementDto) {
        try {
            createDatabase(dataSourceManagementDto.getDbName(), dataSourceManagementDto.getPassword());
        } catch (DataAccessException e) {
            log.error("[createDataSource] ERROR ", e);

            throw new RuntimeException(dataSourceManagementDto.getDbName() + " DB를 생성하는 도중 에러가 발생했습니다.");
        }

        try (Connection connection = DriverManager.getConnection(dataSourceManagementDto.getUrl() + dataSourceManagementDto.getDbName(), dataSourceManagementDto.getDbName(), dataSourceManagementDto.getPassword())) {
            DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
            runLiquibase(tenantDataSource);
        } catch (SQLException | LiquibaseException e) {
            log.error("[createDataSource] ERROR ", e);

            throw new RuntimeException(dataSourceManagementDto.getDbName() + " DB를 생성하는 도중 에러가 발생했습니다.");
        }

        repository.save(DataSourceManagement.builder()
                .tenantId(dataSourceManagementDto.getTenantId())
                .url(dataSourceManagementDto.getUrl())
                .dbName(dataSourceManagementDto.getDbName())
                .password(dataSourceManagementDto.getPassword())
                .build());
    }

    private void createDatabase(String dbName, String password) {
        try {
            jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE DATABASE " + dbName));
        } catch (Exception e) {
            log.error("[createDatabase create db] {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE USER " + dbName + " WITH PASSWORD '" + password + "'"));
        } catch (Exception e) {
            log.error("[createDatabase create user] {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + dbName));
        } catch (Exception e) {
            log.error("[createDatabase privilege db] {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("ALTER DATABASE " + dbName + " OWNER TO " + dbName));
        } catch (Exception e) {
            log.error("[createDatabase alter] {}", e.getMessage());
        }
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
