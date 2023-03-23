package com.tistory.jaimemin.multidatasourcejpa.tenant.repository;

import com.tistory.jaimemin.multidatasourcejpa.tenant.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
