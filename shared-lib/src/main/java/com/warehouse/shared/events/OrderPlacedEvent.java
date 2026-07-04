package com.warehouse.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    
    private Long orderId;
    private String orderNumber;
    private List<OrderItemEvent> items;
    private BigDecimal totalAmount;
    private LocalDateTime ocurredAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}