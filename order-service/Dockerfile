# Stage 1
FROM maven:3.9.9-amazoncorretto-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package --batch-mode -DskipTests

# Stage 2
FROM amazoncorretto:21-alpine3.21-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar order-service.jar

ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]