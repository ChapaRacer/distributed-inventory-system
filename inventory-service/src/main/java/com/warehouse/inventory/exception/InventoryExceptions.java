package com.warehouse.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InventoryExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(Long id) {
            super("Product not found with id: " + id);
        }
        public ProductNotFoundException(String sku) {
            super("Product not found with SKU: " + sku);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String sku, int available, int requested) {
            super(String.format(
                "Insufficient stock for SKU '%s'. Available: %d, Requested: %d",
                sku, available, requested
            ));
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class DuplicateSkuException extends RuntimeException {
        public DuplicateSkuException(String sku) {
            super("A product with SKU '" + sku + "' already exists");
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class StockNotFoundException extends RuntimeException {
        public StockNotFoundException(Long productId, Long warehouseId) {
            super(String.format(
                "No stock record found for productId=%d in warehouseId=%d",
                productId, warehouseId
            ));
        }
    }
}