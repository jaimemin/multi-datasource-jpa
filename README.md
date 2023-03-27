# multi-datasource-jpa
---
서버를 기동하기 전 아래 내용이 선제적으로 적용되어 있어야 합니다.

PostgreSQL > multi_tenancy_master 데이터베이스  data_source_config 테이블 생성

```
create table data_source_config
(
    tenant_id         varchar not null
        constraint key_name
            primary key,
    url               varchar,
    db_name           varchar,
    username          varchar,
    password          varchar,
    driver_class_name varchar
);

alter table data_source_config
    owner to postgres;
```

프로젝트 관련 상세 설명: https://jaimemin.tistory.com/2270
