FROM maven:3.8.3-openjdk-17-slim AS build

WORKDIR /app

COPY ./pom.xml .

RUN mvn package verify clean --fail-never

COPY ./src ./src

RUN mvn -Dmaven.test.skip package

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build app/target/moviebookingapp-0.0.1-SNAPSHOT.jar ./

COPY ./logback-spring.xml /deployments/ 

EXPOSE 8088

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "moviebookingapp-0.0.1-SNAPSHOT.jar"]

LABEL name = "moviebookingapp"