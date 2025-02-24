# Build stage
FROM openjdk:17-alpine AS builder
WORKDIR /app
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build

# Run stage

FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8081