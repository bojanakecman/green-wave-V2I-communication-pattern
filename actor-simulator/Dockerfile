FROM maven:3.6.3-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
FROM openjdk:8-jdk-alpine
COPY --from=build /home/app/target/actor-simulator-0.0.1-SNAPSHOT.jar /actor-simulator.jar
CMD java -jar /actor-simulator.jar