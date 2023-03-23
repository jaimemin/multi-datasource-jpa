package com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "data_source_config")
public class DataSourceManagement {

    @Id
    private String tenantId;

    private String url;

    private String dbName;

    private String password;
}
