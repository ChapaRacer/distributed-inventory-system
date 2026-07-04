package com.warehouse.shared.constants;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String INVENTORY_STOCK_UPDATED = "inventory.stock.updated";
    public static final String INVENTORY_LOW_STOCK     = "inventory.low.stock";
    public static final String ORDER_PLACED            = "order.placed";
    public static final String ORDER_CANCELLED         = "order.cancelled";
    public static final String ORDER_STATUS_CHANGED    = "order.status.changed";
}
