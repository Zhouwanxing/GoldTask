FROM openjdk:8
ARG JAR_FILE=*.jar
ENTRYPOINT ["java","-jar","GoldTask.jar"]
EXPOSE 10000