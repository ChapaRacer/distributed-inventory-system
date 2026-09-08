package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(String category);

    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN p.stockLevels s
        WHERE s.quantity <= s.lowStockThreshold        
        """)
    List<Product> findProductsWithLowStock();

    @Query("""
        SELECT p FROM Product p
        JOIN p.stockLevels s
        WHERE s.warehouse.id = :warehouseId
        """)
    List<Product> findByWarehouseId(@Param("warehouseId") Long warehouseId);
}
