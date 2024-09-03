#FROM maven:3-openjdk-8 as builder
#
#FROM openjdk:8
#ARG JAR_FILE=target/*.jar
#ADD ./target/GoldTask.jar GoldTask.jar
#ENTRYPOINT ["java","-jar","/GoldTask.jar"]
#EXPOSE 10000


FROM maven:alpine

ENV TZ=Asia/Shanghai

WORKDIR /code

COPY settings.xml /usr/share/maven/conf/settings.xml

COPY . .

#MVN 打包
RUN ["mvn","package"]

#执行java -jar启动命令
ENTRYPOINT ["java", "-jar","target/GoldTask.jar"]
EXPOSE 10000