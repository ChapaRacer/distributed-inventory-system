# Developer Guide

## Requirements

- Docker Desktop installed
- That's it - no Java or Maven are needed to run the project

## Run the project

```bash
docker compose up --build
```

First run takes 5-10 minutes. Subsequent runs take ~30 seconds.

When you see this line, the system is ready:

```bash
warehouse-api-gateway | Started ApiGatewayApplication
```

## Available endpoints

All requests go through the API gateway on port 8080.

### Products

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/products | List all products |
| GET | /api/products/{id} | Get product by ID (Redis cached) |
| POST | /api/products | Create a product |
| PUT | /api/products/{id}/stock | Update stock level |
| DELETE | /api/products/{id} | Delete a product |
| GET | /api/products/low-stock | List products below threshold |
| GET | /api/products/search?name= | Search by name |

### Orders

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/orders | List all orders |
| GET | /api/orders/{id} | Get order by ID |
| POST | /api/orders | Place a new order |
| PUT | /api/orders/{id}/cancel | Cancel an order |

## Kafka event flow

order-service -> [order.placed] -> inventory-service(deducts stock)

order-service -> [order.placed] -> notification-service (logs confirmation)

order-service -> [order.cancelled] -> inventory-service (returns stock)

inventory-service -> [inventory.stock.updated] -> notification-service (logs change)

inventory-service -> [inventory.low.stock] -> notification-service (logs alert)

## Inspect Redis cache

```bash
docker exec -it warehouse-redis redis-cli
KEYS *
GET "products::1"
TTL "products::1"
```
## Inspect Kafka topics

```bash
docker exec -it warehouse-kafka bush
kafka-topics --bootstrap-server localhost:9092 --list
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order.placed \
  --from-beginning
```


## Inspect the database

```bash
docker exec -it warehouse-postgres psql -U warehouse_user -d warehouse_db

SELECT p.sku, p.name, s.quantity
DROM inventory.stock s
JOIN inventory.products p ON s.product_id = p.id;

SELECT order_number, status, total_amount
FROM orders.orders
ORDER BY created_at DESC;
```

## Reset eveything

```bash
# Destroy all containers and data
docker compose down -v

# Start fresh
docker compose up --build
```