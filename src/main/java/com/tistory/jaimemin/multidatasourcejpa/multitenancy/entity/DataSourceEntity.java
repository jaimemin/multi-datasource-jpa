package com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceEntity {

    @Id
    private String tenantId;

    private String url;

    private String dbName;

    private String password;
}
