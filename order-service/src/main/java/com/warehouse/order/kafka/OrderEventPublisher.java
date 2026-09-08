package com.warehouse.order.kafka;

import com.warehouse.order.entity.Order;
import com.warehouse.shared.constants.KafkaTopics;
import com.warehouse.shared.events.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(Order order) {
        OrderPlacedEvent event = buildEvent(order);
        kafkaTemplate.send(KafkaTopics.ORDER_PLACED, order.getOrderNumber(), event);
        log.info("Published order.placed event: {}", order.getOrderNumber());
    }

    public void publishOrderCancelled(Order order) {
        OrderPlacedEvent event = buildEvent(order);
        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, order.getOrderNumber(), event);
        log.info("Published order.cancelled event: {}", order.getOrderNumber());
    }

    private OrderPlacedEvent buildEvent(Order order) {
        var eventItems = order.getItems().stream()
            .map(item -> OrderPlacedEvent.OrderItemEvent.builder()
                .productId(item.getProductId())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build())
            .collect(Collectors.toList());

        return OrderPlacedEvent.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .items(eventItems)
            .totalAmount(order.getTotalAmount())
            .occurredAt(LocalDateTime.now())
            .build();
    }   
}