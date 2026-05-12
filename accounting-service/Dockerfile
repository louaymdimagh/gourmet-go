FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY proto ../proto
COPY accounting-service/pom.xml .
COPY accounting-service/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8083 9093
ENTRYPOINT ["java", "-jar", "app.jar"]
