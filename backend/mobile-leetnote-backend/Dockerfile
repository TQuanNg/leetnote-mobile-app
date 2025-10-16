# Step 1: Build stage
FROM gradle:8.14.2-jdk24 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle bootJar --no-daemon

# Step 2: Run stage (lighter JDK image)
FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]