FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY shared-lib/ ./shared-lib/
RUN cd shared-lib && mvn install -q -DskipTests
COPY order-service/ ./order-service/
RUN cd order-service && mvn package -q -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/order-service/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]