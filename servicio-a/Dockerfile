# Stage 1: Build inside Docker using Maven and Java 17
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml common/
COPY servicio-a/pom.xml servicio-a/
COPY servicio-b/pom.xml servicio-b/
COPY servicio-c/pom.xml servicio-c/

COPY common/src common/src
COPY servicio-a/src servicio-a/src

RUN mvn clean package -pl common,servicio-a -am -DskipTests

# Stage 2: Extract layers for fast container startup
FROM eclipse-temurin:17-jre-jammy AS extractor
WORKDIR /extracted
COPY --from=builder /build/servicio-a/target/servicio-a-1.0.0-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Minimal production runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring -u 10001 spring

COPY --from=extractor /extracted/dependencies/ ./
COPY --from=extractor /extracted/spring-boot-loader/ ./
COPY --from=extractor /extracted/snapshot-dependencies/ ./
COPY --from=extractor /extracted/application/ ./

RUN chown -R spring:spring /app
USER spring:spring

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
