# syntax=docker/dockerfile:1

# ---- Build: compila o jar com o Maven Wrapper ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Dependências primeiro, em camada própria: só são baixadas de novo se o pom mudar
COPY .mvn/ .mvn/
COPY mvnw pom.xml lombok.config ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Os testes (unitários, integração e cobertura) rodam no `./mvnw verify` local/CI
RUN ./mvnw -B -q package -DskipTests && cp target/delivery-*.jar app.jar

# ---- Runtime: só a JRE e o jar, executando com usuário sem privilégios ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app --no-create-home app \
    && mkdir -p /app/data \
    && chown app:app /app/data

COPY --from=build /workspace/app.jar app.jar

USER app

# Banco SQLite em /app/data, que deve ser montado como volume para persistir
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/app/data/foody.db \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

VOLUME /app/data
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
