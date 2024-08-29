FROM maven:3-openjdk-8 as builder

FROM openjdk:8
ARG JAR_FILE=target/*.jar
ADD ./target/GoldTask.jar GoldTask.jar
ENTRYPOINT ["java","-jar","/GoldTask.jar"]
EXPOSE 10000