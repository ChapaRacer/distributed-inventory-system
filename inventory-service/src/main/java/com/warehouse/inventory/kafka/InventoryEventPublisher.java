package com.warehouse.inventory.kafka;

import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.Stock;
import com.warehouse.shared.constants.KafkaTopics;
import com.warehouse.shared.events.StockUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockUpdated(Product product, Stock stock, int previousQty, String reason) {
        StockUpdatedEvent event = StockUpdatedEvent.builder()
                .productId(product.getId())
                .productSku(product.getSku())
                .productName(product.getName())
                .warehouseId(stock.getWarehouse().getId())
                .warehouseName(stock.getWarehouse().getName())
                .previousQuantity(previousQty)
                .newQuantity(stock.getQuantity())
                .quantityDelta(stock.getQuantity() - previousQty)
                .reason(reason != null ? reason : "UNSPECIFIED")
                .belowThreshold(stock.isBelowThreshold())
                .occurredAt(LocalDateTime.now())
                .build();

        sendEvent(KafkaTopics.INVENTORY_STOCK_UPDATED, product.getSku(), event);
    }

    public void publishLowStockAlert(Product product, Stock stock) {
        StockUpdatedEvent event = StockUpdatedEvent.builder()
                .productId(product.getId())
                .productSku(product.getSku())
                .productName(product.getName())
                .warehouseId(stock.getWarehouse().getId())
                .warehouseName(stock.getWarehouse().getName())
                .newQuantity(stock.getQuantity())
                .belowThreshold(true)
                .occurredAt(LocalDateTime.now())
                .build();

        sendEvent(KafkaTopics.INVENTORY_LOW_STOCK, product.getSku(), event);
    }

    private void sendEvent(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic '{}' with key '{}': {}",
                        topic, key, ex.getMessage());
            } else {
                log.debug("Event published to topic='{}', key='{}', partition={}, offset={}",
                        topic, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}