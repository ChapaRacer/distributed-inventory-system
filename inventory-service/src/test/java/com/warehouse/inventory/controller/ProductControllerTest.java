package com.warehouse.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.inventory.dto.ProductDto;
import com.warehouse.inventory.exception.InventoryExceptions.ProductNotFoundException;
import com.warehouse.inventory.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Web Layer Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductDto.Response sampleResponse() {
        return ProductDto.Response.builder()
                .id(1L)
                .sku("LAPTOP-PRO-16")
                .name("Laptop Pro 16\"")
                .unitPrice(new BigDecimal("25000.00"))
                .category("Electronics")
                .stockLevels(List.of())
                .build();
    }

    @Test
    @DisplayName("GET /api/products - returns 200 with list")
    void getAllProducts_returns200WithList() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/products")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].sku").value("LAPTOP-PRO-16"));
    }

    @Test
    @DisplayName("GET /api/products/1 - existing product - returns 200")
    void getProductById_exists_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.sku").value("LAPTOP-PRO-16"));
    }

    @Test
    @DisplayName("GET /api/products/999 - not found - returns 404")
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    @DisplayName("POST /api/products - valid request - returns 201")
    void createProduct_validRequest_returns201() throws Exception {
        ProductDto.CreateRequest request = ProductDto.CreateRequest.builder()
                .sku("MOUSE-WIRELESS")
                .name("Wireless Mouse")
                .unitPrice(new BigDecimal("450.00"))
                .category("Peripherals")
                .initialStock(0)
                .build();

        when(productService.createProduct(any())).thenReturn(
            ProductDto.Response.builder()
                .id(2L)
                .sku("MOUSE-WIRELESS")
                .name("Wireless Mouse")
                .unitPrice(new BigDecimal("450.00"))
                .build()
        );

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.sku").value("MOUSE-WIRELESS"));
    }

    @Test
    @DisplayName("POST /api/products - blank SKU - returns 400")
    void createProduct_blankSku_returns400() throws Exception {
        ProductDto.CreateRequest invalidRequest = ProductDto.CreateRequest.builder()
                .sku("")
                .name("Some Product")
                .unitPrice(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("DELETE /api/products/1 - returns 204")
    void deleteProduct_exists_returns204() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }
}