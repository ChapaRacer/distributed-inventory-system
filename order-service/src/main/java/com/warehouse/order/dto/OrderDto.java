package com.warehouse.order.dto;

import com.warehouse.order.entity.Order.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class CreateRequest {
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        private List<OrderItemRequest> items;
        private String notes;
    }

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotBlank(message = "Product SKU is required")
        private String productSku;

        @NotBlank(message = "Product name is required")
        private String productName;

        @NotNull(message = "Quantity is require")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 9999, message = "Quantity cannot exceed 9999 per line item")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal unitPrice;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String orderNumber;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private String notes;
        private List<OrderItemResponse> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productSku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}