# Stage 1: Build
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY proto ../proto
COPY order-service/pom.xml .
COPY order-service/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081 9091
ENTRYPOINT ["java", "-jar", "app.jar"]
