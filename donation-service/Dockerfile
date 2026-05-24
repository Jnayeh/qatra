FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -B -DskipTests

RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# ── Minimal JRE (Alpine-native, no glibc needed) ───────────
FROM eclipse-temurin:21-jdk-alpine AS jlink
RUN jlink --add-modules java.base,java.logging,java.xml,jdk.unsupported,java.sql,java.naming,java.management,jdk.crypto.ec --strip-debug --no-man-pages --no-header-files --compress=zip-9 --output /jre

# ── Runtime stage ──────────────────────────────────────────
FROM alpine:3.19
RUN apk add --no-cache ca-certificates

RUN addgroup -g 1000 appgroup && adduser -D -u 1000 -G appgroup appuser

COPY --from=jlink /jre /jre
COPY --from=build /app/extracted/dependencies/ /workspace/dependencies/
COPY --from=build /app/extracted/spring-boot-loader/ /workspace/spring-boot-loader/
COPY --from=build /app/extracted/snapshot-dependencies/ /workspace/snapshot-dependencies/
COPY --from=build /app/extracted/application/ /workspace/application/

RUN rm -rf /jre/legal /jre/conf/management

RUN chown -R appuser:appgroup /workspace

USER appuser
WORKDIR /workspace

ENV JAVA_HOME=/jre
ENV PATH="$JAVA_HOME/bin:$PATH"

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
