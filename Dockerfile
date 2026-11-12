# --- Estágio 1: Build ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

# 1. Copia apenas o descritor de dependências primeiro
COPY pom.xml .

# 2. Baixa todas as dependências (o Docker guarda em cache se o pom.xml não mudar)
RUN mvn dependency:go-offline -B

# 3. Agora sim, copia o código-fonte da aplicação
COPY src ./src

# 4. Compila o projeto
RUN mvn -B -DskipTests package


# --- Estágio 2: Execução ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o jar gerado no estágio anterior
COPY --from=build /workspace/target/loginApp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]