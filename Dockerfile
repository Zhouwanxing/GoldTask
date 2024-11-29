#FROM maven:3-openjdk-8 as builder
#
#FROM openjdk:8
#ARG JAR_FILE=target/*.jar
#ADD ./target/GoldTask.jar GoldTask.jar
#ENTRYPOINT ["java","-jar","/GoldTask.jar"]
#EXPOSE 10000


FROM maven:alpine

ENV TZ=Asia/Shanghai


# 设置环境变量，避免交互提示
ENV DEBIAN_FRONTEND=noninteractive

RUN apk add --no-cache \
    bash \
    curl \
    wget \
    unzip \
    libstdc++ \
    nss \
    freetype \
    harfbuzz \
    ttf-freefont \
    udev \
    mesa-gl \
    xvfb

# 下载并安装 Chrome 二进制文件
ARG CHROME_VERSION=131.0.6778.85
RUN wget -O /tmp/chrome-linux64.zip https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/linux64/chrome-linux64.zip && \
    unzip /tmp/chrome-linux64.zip -d /opt/ && \
    mv /opt/chrome-linux64 /opt/chrome && \
    ln -s /opt/chrome/chrome /usr/bin/google-chrome && \
    rm /tmp/chrome-linux64.zip

# 下载并安装 ChromeDriver（与 Chrome 版本一致）
RUN unzip chromedriver.zip -d /usr/ && \
    chmod +x /usr/chromedriver

WORKDIR /code

#COPY settings.xml /usr/share/maven/conf/settings.xml

COPY . .

#MVN 打包
RUN ["mvn","package"]

#执行java -jar启动命令
ENTRYPOINT ["java", "-Xms256m", "-Xmx500m", \
                   "-XX:MetaspaceSize=64m", "-XX:MaxMetaspaceSize=128m", \
                   "-XX:+UseSerialGC", \
                   "-XX:NewRatio=3", \
                   "-Xss256k", "-jar","target/GoldTask.jar"]
EXPOSE 10000