package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.ProductDto;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.Stock;
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.exception.InventoryExceptions.*;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Transactional
    public ProductDto.Response createProduct(ProductDto.CreateRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .unitPrice(request.getUnitPrice())
                .category(request.getCategory())
                .build();

        product = productRepository.save(product);

        if (request.getInitialStock() != null && request.getInitialStock() > 0) {
            Warehouse defaultWarehouse = new Warehouse();
            defaultWarehouse.setId(1L);

            Stock stock = Stock.builder()
                    .product(product)
                    .warehouse(defaultWarehouse)
                    .quantity(request.getInitialStock())
                    .lowStockThreshold(10)
                    .build();
            stockRepository.save(stock);
        }

        return toResponse(productRepository.findById(product.getId()).orElseThrow());
    }

    @Cacheable(cacheNames = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductDto.Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Response> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Response> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Response> getLowStockProducts() {
        return productRepository.findProductsWithLowStock()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "products", key = "#productId")
    public ProductDto.Response updateStock(Long productId, ProductDto.StockUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Stock stock = stockRepository.findByProductIdAndWarehouseIdForUpdate(
                productId, request.getWarehouseId())
                .orElseThrow(() -> new StockNotFoundException(productId, request.getWarehouseId()));

        int previousQty = stock.getQuantity();

        if (request.getQuantityDelta() > 0) {
            stock.add(request.getQuantityDelta());
        } else {
            stock.deduct(Math.abs(request.getQuantityDelta()));
        }

        stockRepository.save(stock);
        return toResponse(productRepository.findById(productId).orElseThrow());
    }

    @Transactional
    @CacheEvict(cacheNames = "products", key = "#id")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private ProductDto.Response toResponse(Product product) {
        List<ProductDto.StockInfo> stockInfos = product.getStockLevels()
                .stream()
                .map(s -> ProductDto.StockInfo.builder()
                        .warehouseId(s.getWarehouse().getId())
                        .warehouseName(s.getWarehouse().getName())
                        .quantity(s.getQuantity())
                        .lowStockThreshold(s.getLowStockThreshold())
                        .belowThreshold(s.isBelowThreshold())
                        .build())
                .collect(Collectors.toList());

        return ProductDto.Response.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .unitPrice(product.getUnitPrice())
                .category(product.getCategory())
                .stockLevels(stockInfos)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}