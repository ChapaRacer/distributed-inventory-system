package com.warehouse.notification.kafka;

import com.warehouse.shared.constants.KafkaTopics;
import com.warehouse.shared.events.OrderPlacedEvent;
import com.warehouse.shared.events.StockUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventConsumer {
    
    @KafkaListener(
        topics = KafkaTopics.INVENTORY_LOW_STOCK,
        groupId = "notification-service-group"
    )
    public void handleLowStock(StockUpdatedEvent event) {
        log.warn("LOW STOCK ALERT - Product: {} ({}), Warehouse: {}, Current qty: {}",
            event.getProductName(),
            event.getProductSku(),
            event.getWarehouseName(),
            event.getNewQuantity()
        );
    }

    @KafkaListener(
        topics = KafkaTopics.INVENTORY_STOCK_UPDATED,
        groupId = "notification-service-group"
    )
    public void handleStockUpdated(StockUpdatedEvent event) {
        log.info("Stock updated - Product: {} | Before: {} -> After: {} | Reason: {}",
            event.getProductSku(),
            event.getPreviousQuantity(),
            event.getNewQuantity(),
            event.getReason()
        );
    }

    @KafkaListener(
        topics = KafkaTopics.ORDER_PLACED,
        groupId = "notification-service-group"
    )
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("New order placed - Order: {}, Items: {}, Total: ${}",
            event.getOrderNumber(),
            event.getItems().size(),
            event.getTotalAmount()
        );
    }

    @KafkaListener(
        topics = KafkaTopics.ORDER_CANCELLED,
        groupId = "notification-service-group"
    )
    public void handleOrderCancelled(OrderPlacedEvent event) {
        log.info("Order cancelled - Order: {}, Items: {}",
            event.getOrderNumber(),
            event.getItems().size()
        );
    }
}