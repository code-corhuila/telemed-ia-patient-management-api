# syntax=docker/dockerfile:1.7

# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy the parent POM and all module POMs first, so dependency resolution
# is cached across rebuilds even when only the sources change.
COPY pom.xml ./
COPY patient-core/pom.xml patient-core/pom.xml
COPY patient-adapters/pom.xml patient-adapters/pom.xml
COPY patient-app/pom.xml patient-app/pom.xml
RUN mvn -B -q -pl patient-core,patient-adapters,patient-app -am dependency:go-offline

# Copy the sources of every module and build the fat jar of patient-app.
COPY patient-core/src patient-core/src
COPY patient-adapters/src patient-adapters/src
COPY patient-app/src patient-app/src
RUN mvn -B -q -pl patient-app -am clean package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/patient-app/target/patient-app-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]