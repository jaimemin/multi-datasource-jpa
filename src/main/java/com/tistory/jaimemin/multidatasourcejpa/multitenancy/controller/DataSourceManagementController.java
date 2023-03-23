package com.tistory.jaimemin.multidatasourcejpa.multitenancy.controller;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.dto.DataSourceManagementDto;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.entity.DataSourceManagement;
import com.tistory.jaimemin.multidatasourcejpa.multitenancy.service.DataSourceManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataSource")
public class DataSourceManagementController {

    private final DataSourceManagementService dataSourceManagementService;

    @GetMapping
    public ResponseEntity<?> getAllDataSources() {
        try {
            return ResponseEntity.ok(dataSourceManagementService.findALl());
        } catch (Exception e) {
            throw new RuntimeException("테넌트 정보들을 불러오는데 실패했습니다");
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody DataSourceManagementDto dto) {
        try {
            DataSourceManagement byTenantId = dataSourceManagementService.findByTenantId(dto.getTenantId());

            if (!ObjectUtils.isEmpty(byTenantId)) {
                return ResponseEntity.internalServerError().body("이미 존재하는 테넌트입니다. (" + dto.getTenantId() + ")");
            }

            dataSourceManagementService.createDataSource(dto);

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("[DataSourceManagementController.save] ERROR ", e);

            throw new RuntimeException("테넌트 등록에 실패했습니다");
        }
    }
}
