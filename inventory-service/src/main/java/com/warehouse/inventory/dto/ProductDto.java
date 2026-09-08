package com.warehouse.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDto {

    @Data
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must be 50 characters or less")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
        private String sku;

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Name must be 150 characters or less")
        private String name;

        private String description;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price format: max 8 integer digits and 2 decimal places")
        private BigDecimal unitPrice;

        private String category;

        @Min(value = 0, message = "Initial stock cannot be negative")
        private Integer initialStock = 0;
    }

    @Data
    public static class StockUpdateRequest {

        @NotNull(message = "Warehouse ID is required")
        private Long warehouseId;

        @NotNull(message = "Quantity change is required")
        private Integer quantityDelta;

        private String reason;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private BigDecimal unitPrice;
        private String category;
        private List<StockInfo> stockLevels;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class StockInfo {
        private Long warehouseId;
        private String warehouseName;
        private Integer quantity;
        private Integer lowStockThreshold;
        private boolean belowThreshold;
    }
}