# Build stage
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17.0.13-al2023-headless
WORKDIR /app
COPY --from=build /app/target/*.jar project2.jar
EXPOSE 8080
CMD ["java", "-jar", "project2.jar"]
