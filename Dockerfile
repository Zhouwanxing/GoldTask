FROM openjdk:8
ARG JAR_FILE=*.jar
ADD ./GoldTask.jar GoldTask.jar
ENTRYPOINT ["java","-jar","/GoldTask.jar"]
EXPOSE 10000