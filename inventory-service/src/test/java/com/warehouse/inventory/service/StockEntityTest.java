package com.warehouse.inventory.service;

import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.Stock;
import com.warehouse.inventory.entity.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Stock Entity Tests")
class StockEntityTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .id(1L)
                .sku("KEYBOARD-MECH")
                .name("Mechanical Keyboard")
                .unitPrice(new BigDecimal("1200.00"))
                .build();

        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .name("Main Warehouse")
                .build();

        stock = Stock.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .quantity(50)
                .lowStockThreshold(10)
                .build();
    }

    @Test
    @DisplayName("deduct - valid amount - reduces quantity correctly")
    void deduct_validAmount_reducesQuantity() {
        stock.deduct(15);
        assertThat(stock.getQuantity()).isEqualTo(35);
    }

    @Test
    @DisplayName("deduct - exact quantity - reduces to zero")
    void deduct_exactQuantity_reducesToZero() {
        stock.deduct(50);
        assertThat(stock.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("deduct - more than available - throws IllegalStateException")
    void deduct_moreThanAvailable_throwsException() {
        assertThatThrownBy(() -> stock.deduct(51))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("add - positive amount - increases quantity")
    void add_positiveAmount_increasesQuantity() {
        stock.add(20);
        assertThat(stock.getQuantity()).isEqualTo(70);
    }

    @Test
    @DisplayName("isBelowThreshold - quantity above threshold - returns false")
    void isBelowThreshold_aboveThreshold_returnsFalse() {
        assertThat(stock.isBelowThreshold()).isFalse();
    }

    @Test
    @DisplayName("isBelowThreshold - quantity at threshold - returns true")
    void isBelowThreshold_atThreshold_returnsTrue() {
        stock.deduct(40);
        assertThat(stock.isBelowThreshold()).isTrue();
    }

    @Test
    @DisplayName("deduct then add - net quantity correct")
    void deductThenAdd_netQuantityIsCorrect() {
        stock.deduct(30);
        assertThat(stock.getQuantity()).isEqualTo(20);
        stock.add(30);
        assertThat(stock.getQuantity()).isEqualTo(50);
    }
}