package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.product.id = : productId AND s.warehouse.id = :warehouseId")
    Optional<Stock> findByProductIdAndWarehouseIdForUpdate(
        @Param("productId") Long productId,
        @Param("warehouseId") Long warehouseId
    );
}   
