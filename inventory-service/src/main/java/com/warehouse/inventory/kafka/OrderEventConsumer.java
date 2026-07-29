package com.warehouse.inventory.kafka;

import com.warehouse.inventory.entity.Stock;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.StockRepository;
import com.warehouse.shared.constants.KafkaTopics;
import com.warehouse.shared.events.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final InventoryEventPublisher eventPublisher;

    @KafkaListener(
        topics = KafkaTopics.ORDER_PLACED,
        groupId = "inventory-service-group"
    )
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order.placed event: orderNumber={}, items={}",
                event.getOrderNumber(), event.getItems().size());

        for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
            try {
                reserveStock(item, event.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to reserve stock for SKU={}, orderId={}: {}",
                        item.getProductSku(), event.getOrderId(), e.getMessage());
            }
        }
    }

    @KafkaListener(
        topics = KafkaTopics.ORDER_CANCELLED,
        groupId = "inventory-service-group"
    )
    @Transactional
    public void handleOrderCancelled(OrderPlacedEvent event) {
        log.info("Received order.cancelled event: orderNumber={}", event.getOrderNumber());

        for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
            stockRepository.findByProductIdAndWarehouseIdForUpdate(item.getProductId(), 1L)
                    .ifPresent(stock -> {
                        int previousQty = stock.getQuantity();
                        stock.add(item.getQuantity());
                        stockRepository.save(stock);

                        productRepository.findById(item.getProductId()).ifPresent(product ->
                            eventPublisher.publishStockUpdated(
                                product, stock, previousQty, "ORDER_CANCELLED"
                            )
                        );

                        log.info("Stock returned for SKU={}: +{} units",
                                item.getProductSku(), item.getQuantity());
                    });
        }
    }

    private void reserveStock(OrderPlacedEvent.OrderItemEvent item, String orderNumber) {
        Stock stock = stockRepository.findByProductIdAndWarehouseIdForUpdate(
                item.getProductId(), 1L)
                .orElseThrow(() -> new RuntimeException(
                    "No stock found for productId=" + item.getProductId()
                ));

        int previousQty = stock.getQuantity();
        stock.deduct(item.getQuantity());
        stockRepository.save(stock);

        productRepository.findById(item.getProductId()).ifPresent(product -> {
            eventPublisher.publishStockUpdated(
                product, stock, previousQty, "ORDER_RESERVATION::" + orderNumber
            );
            if (stock.isBelowThreshold()) {
                eventPublisher.publishLowStockAlert(product, stock);
            }
        });

        log.info("Stock reserved for SKU={}: -{} units (order {})",
                item.getProductSku(), item.getQuantity(), orderNumber);
    }
}