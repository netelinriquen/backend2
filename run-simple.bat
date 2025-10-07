@echo off
echo Compilando InkFlow API...

if not exist "build" mkdir build

if not exist "postgresql.jar" (
    echo Baixando driver PostgreSQL...
    curl -o postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.1.jar
)

javac -cp postgresql.jar -d build src/main/java/com/inkflow/api/SimpleApplication.java

echo Executando InkFlow API na porta 8080...
java -cp "build;postgresql.jar" com.inkflow.api.SimpleApplication

pause