package com.warehouse.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdatedEvent {
    
    private Long productId;
    private String productSku;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer quantityDelta;
    private String reason;
    private boolean belowThreshold;
    private LocalDateTime occurredAt;
}
