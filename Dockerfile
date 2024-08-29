#FROM maven:3-openjdk-8 as builder
#
#FROM openjdk:8
#ARG JAR_FILE=target/*.jar
#ADD ./target/GoldTask.jar GoldTask.jar
#ENTRYPOINT ["java","-jar","/GoldTask.jar"]
#EXPOSE 10000


FROM maven:alpine
#指定环境变量url为/usr/helloworld
ENV url /usr/GoldTask

#指定容器的工作目录
WORKDIR ${url}

#复制helloworld下的所有文件到镜像中的/usr/helloworld目录中
COPY GoldTask ${url}

#把maven镜像中的maven配置文件settings.xml，替换为自己的settings.xml，为了加快下载
COPY /usr/GoldTask/settings.xml /usr/share/maven/conf/settings.xml

#MVN 打包
RUN ["mvn","package"]

#执行java -jar启动命令
ENTRYPOINT ["java", "-jar","target/GoldTask.jar"]
EXPOSE 10000