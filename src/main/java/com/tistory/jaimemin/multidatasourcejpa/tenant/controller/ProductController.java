package com.tistory.jaimemin.multidatasourcejpa.tenant.controller;

import com.tistory.jaimemin.multidatasourcejpa.tenant.entity.Product;
import com.tistory.jaimemin.multidatasourcejpa.tenant.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private static final String TENANT_ID = "x-tenant-id";

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> save(HttpServletRequest request, @RequestBody Product product) {
        try {
            String tenantId = request.getHeader(TENANT_ID);
            product.setCreatedBy(tenantId);
            productService.createProduct(product);

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> delete(@PathVariable Integer productId) {
        try {
            productService.deleteProductById(productId);

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
