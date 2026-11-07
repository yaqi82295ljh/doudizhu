FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY backend/pom.xml .
RUN mvn -q -B dependency:go-offline

COPY backend/src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
