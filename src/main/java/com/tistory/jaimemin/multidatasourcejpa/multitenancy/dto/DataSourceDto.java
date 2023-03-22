package com.tistory.jaimemin.multidatasourcejpa.multitenancy.dto;

import lombok.Data;

@Data
public class DataSourceDto {

    private String tenantId;

    private String url;

    private String dbName;

    private String password;
}
