FROM openjdk:17-jdk-slim

WORKDIR /app

COPY src/ ./src/

RUN javac -d build src/main/java/com/inkflow/api/SimpleApplication.java

EXPOSE 8080

CMD ["java", "-cp", "build", "com.inkflow.api.SimpleApplication"]