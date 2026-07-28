package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.ProductDto;
import com.warehouse.inventory.service.ProductService;
import com.warehouse.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> getAllProducts() {
        List<ProductDto.Response> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.ok(products, "Found " + products.size() + " products"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto.Response>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto.Response>> createProduct(
            @Valid @RequestBody ProductDto.CreateRequest request) {
        ProductDto.Response created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Product created successfully"));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductDto.Response>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto.StockUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            productService.updateStock(id, request), "Stock updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> getLowStockProducts() {
        List<ProductDto.Response> products = productService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.ok(products, "Found " + products.size() + " products with low stock"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> searchByName(
            @RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok(productService.searchByName(name)));
    }
}