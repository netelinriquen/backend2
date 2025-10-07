@echo off
echo Compilando e executando InkFlow API...

if not exist "build" mkdir build
if not exist "build\classes" mkdir build\classes

echo Baixando dependencias...
curl -o postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.1.jar
curl -o spring-boot.jar https://repo1.maven.org/maven2/org/springframework/boot/spring-boot/3.2.0/spring-boot-3.2.0.jar

echo Compilando...
javac -cp "postgresql.jar;spring-boot.jar" -d build/classes src/main/java/com/inkflow/api/*.java src/main/java/com/inkflow/api/*/*.java

echo Executando...
java -cp "build/classes;postgresql.jar;spring-boot.jar" com.inkflow.api.InkFlowApiApplication