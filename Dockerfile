FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src/ ./src/

# Instalar Maven
RUN apt-get update && apt-get install -y maven

# Build Spring Boot
RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/inkflow-api-1.0.0.jar"]