FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /opt/app

COPY multiplayer-grupp1/.mvn/ .mvn
COPY multiplayer-grupp1/mvnw multiplayer-grupp1/pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY multiplayer-grupp1/src ./src
RUN ./mvnw clean install -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/target/*.jar /opt/app/app.jar

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=prod -jar /opt/app/app.jar"]
