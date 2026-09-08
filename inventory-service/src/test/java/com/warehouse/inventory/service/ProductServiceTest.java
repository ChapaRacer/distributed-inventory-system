package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.ProductDto;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.exception.InventoryExceptions.DuplicateSkuException;
import com.warehouse.inventory.exception.InventoryExceptions.ProductNotFoundException;
import com.warehouse.inventory.kafka.InventoryEventPublisher;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .sku("LAPTOP-PRO-16")
                .name("Laptop Pro 16\"")
                .unitPrice(new BigDecimal("25000.00"))
                .category("Electronics")
                .stockLevels(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("createProduct - valid request - saves and returns product")
    void createProduct_validRequest_returnsCreatedProduct() {
        ProductDto.CreateRequest request = ProductDto.CreateRequest.builder()
                .sku("LAPTOP-PRO-16")
                .name("Laptop Pro 16\"")
                .unitPrice(new BigDecimal("25000.00"))
                .category("Electronics")
                .initialStock(0)
                .build();

        when(productRepository.existsBySku("LAPTOP-PRO-16")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductDto.Response result = productService.createProduct(request);

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("LAPTOP-PRO-16");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct - duplicate SKU - throws DuplicateSkuException")
    void createProduct_duplicateSku_throwsDuplicateSkuException() {
        ProductDto.CreateRequest request = ProductDto.CreateRequest.builder()
                .sku("LAPTOP-PRO-16")
                .name("Another Laptop")
                .unitPrice(new BigDecimal("20000.00"))
                .build();

        when(productRepository.existsBySku("LAPTOP-PRO-16")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("LAPTOP-PRO-16");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("getProductById - existing product - returns product")
    void getProductById_existingId_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductDto.Response result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("LAPTOP-PRO-16");
    }

    @Test
    @DisplayName("getProductById - non-existent ID - throws ProductNotFoundException")
    void getProductById_nonExistentId_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteProduct - existing product - deletes successfully")
    void deleteProduct_existingId_deletesWithoutException() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> productService.deleteProduct(1L))
                .doesNotThrowAnyException();

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteProduct - non-existent ID - throws ProductNotFoundException")
    void deleteProduct_nonExistentId_throwsProductNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("getAllProducts - returns all products")
    void getAllProducts_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<ProductDto.Response> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSku()).isEqualTo("LAPTOP-PRO-16");
    }

    @Test
    @DisplayName("getAllProducts - empty DB - returns empty list")
    void getAllProducts_emptyDb_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductDto.Response> result = productService.getAllProducts();

        assertThat(result).isNotNull().isEmpty();
    }
}