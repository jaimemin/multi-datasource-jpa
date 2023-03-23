package com.tistory.jaimemin.multidatasourcejpa.multitenancy.repository;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity.DataSourceManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataSourceManagementRepository extends JpaRepository<DataSourceManagement, String> {

    Optional<DataSourceManagement> findByTenantId(@Param("tenantId") String tenantId);
}
