FROM maven:3.6.3-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:8-jdk-alpine
COPY --from=build /home/app/target/status-tracking-service-0.0.1-SNAPSHOT.jar /status-tracking-service.jar
CMD java -jar /status-tracking-service.jar