package com.warehouse.order.service;

import com.warehouse.order.dto.OrderDto;
import com.warehouse.order.entity.Order;
import com.warehouse.order.entity.Order.OrderStatus;
import com.warehouse.order.entity.OrderItem;
import com.warehouse.order.kafka.OrderEventPublisher;
import com.warehouse.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public OrderDto.Response placeOrder(OrderDto.CreateRequest request) {
        log.info("Placing order with {} items", request.getItems().size());

        List<OrderItem> items = request.getItems().stream()
            .map(itemReq -> OrderItem.builder()
                .productId(itemReq.getProductId())
                .productSku(itemReq.getProductSku())
                .productName(itemReq.getProductName())
                .quantity(itemReq.getQuantity())
                .unitPrice(itemReq.getUnitPrice())
                .build())
            .collect(Collectors.toList());

        BigDecimal total = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .status(OrderStatus.PENDING)
            .totalAmount(total)
            .notes(request.getNotes())
            .items(items)
            .build();

        items.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderPlaced(saved);

        log.info("Order placed: orderNumber={}, total={}",
                saved.getOrderNumber(), saved.getTotalAmount());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto.Response getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: "+ id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto.Response cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: "+ id));
    
        order.cancel();
        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(saved);

        log.info("Order cancelled: orderNumber={}", saved.getOrderNumber());
        return toResponse(saved);
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        String candidate = "ORD-" + date + "-" + suffix;

        while (orderRepository.existsByOrderNumber(candidate)) {
            suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
            candidate = "ORD-" + date + "-" + suffix;
        }
        return candidate;
    }

    private OrderDto.Response toResponse(Order order) {
        List<OrderDto.OrderItemResponse> itemResponses = order.getItems().stream()
            .map(item -> OrderDto.OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productSku(item.getProductSku())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build())
            .collect(Collectors.toList());

        return OrderDto.Response.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .notes(order.getNotes())
            .items(itemResponses)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}