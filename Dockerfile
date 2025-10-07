FROM openjdk:17-jdk-slim

WORKDIR /app

COPY src/ ./src/

# Baixar driver PostgreSQL
RUN apt-get update && apt-get install -y curl && \
    curl -o postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.1.jar

# Compilar com driver PostgreSQL
RUN javac -cp postgresql.jar -d build src/main/java/com/inkflow/api/SimpleApplication.java

EXPOSE 8080

CMD ["java", "-cp", "build:postgresql.jar", "com.inkflow.api.SimpleApplication"]