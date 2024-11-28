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

RUN yum install -y apt-get

# 更新软件包并安装必要工具和依赖
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    curl \
    libx11-xcb1 \
    libxcomposite1 \
    libxcursor1 \
    libxdamage1 \
    libxi6 \
    libxtst6 \
    libnss3 \
    libxrandr2 \
    libasound2 \
    libpangocairo-1.0-0 \
    libxshmfence1 \
    libgbm1 \
    libglib2.0-0 \
    libgtk-3-0 \
    libgdk-pixbuf2.0-0 \
    libpango-1.0-0 \
    libatk1.0-0 \
    --no-install-recommends

# 下载并安装 Chrome 二进制文件
ARG CHROME_VERSION=131.0.6778.85
RUN wget -O /tmp/chrome-linux64.zip https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/linux64/chrome-linux64.zip && \
    unzip /tmp/chrome-linux64.zip -d /opt/ && \
    mv /opt/chrome-linux64 /opt/chrome && \
    ln -s /opt/chrome/chrome /usr/bin/google-chrome && \
    rm /tmp/chrome-linux64.zip

# 下载并安装 ChromeDriver（与 Chrome 版本一致）
RUN wget -O /tmp/chromedriver.zip https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/linux64/chromedriver-linux64.zip && \
    unzip /tmp/chromedriver.zip -d /usr/local/bin/ && \
    rm /tmp/chromedriver.zip && \
    chmod +x /usr/local/bin/chromedriver

# 清理缓存
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

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