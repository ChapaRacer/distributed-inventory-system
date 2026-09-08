package com.warehouse.order.controller;

import com.warehouse.order.dto.OrderDto;
import com.warehouse.order.service.OrderService;
import com.warehouse.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto.Response>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto.Response>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto.Response>> placeOrder(
        @Valid @RequestBody OrderDto.CreateRequest request) {
            OrderDto.Response order = orderService.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(order, "Order placed successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDto.Response>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
            orderService.cancelOrder(id), "Order cancelled successfully"
        ));
    }
}