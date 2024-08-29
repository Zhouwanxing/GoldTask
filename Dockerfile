FROM maven:3-openjdk-8 as builder

WORKDIR /code

COPY settings.xml /usr/share/maven/conf/settings.xml

COPY . .

RUN mvn -q clean package -Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dmaven.test.skip=true -U

FROM openjdk:8u332
#FROM registry.cn-beijing.aliyuncs.com/holly-common/jdk8

WORKDIR goldTask
COPY --from=0 /code/target/GoldTask.jar .

COPY entrypoint.sh .

ENTRYPOINT ["./entrypoint.sh"]
