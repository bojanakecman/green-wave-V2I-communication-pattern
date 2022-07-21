FROM maven:3.6.3-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:8-jdk-alpine
COPY --from=build /home/app/target/api-gateway-0.0.1-SNAPSHOT.jar /api-gateway.jar
CMD java -jar /api-gateway.jar
EXPOSE 10113
