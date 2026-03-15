# Estágio de Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
# Definimos um diretório de trabalho claro para evitar conflito com pastas do sistema
WORKDIR /app
COPY . .
# Usamos o maven da imagem para compilar
RUN mvn clean package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Buscamos o jar que foi gerado na pasta target do estágio anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]