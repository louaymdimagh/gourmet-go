FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY proto ../proto
COPY order-orchestrator/pom.xml .
COPY order-orchestrator/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
