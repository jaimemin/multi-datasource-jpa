package com.tistory.jaimemin.multidatasourcejpa.multitenancy.repository;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity.DataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DataSourceRepository extends JpaRepository<DataSourceEntity, String> {

    Optional<DataSourceEntity> findByTenantId(@Param("tenantId") String tenantId);
}
