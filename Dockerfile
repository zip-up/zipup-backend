FROM --platform=linux/amd64 adoptopenjdk:11-jdk-hotspot

RUN ln -sf /usr/share/zoneinfo/Asiz/Seoul /etc/localtime

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

#FROM --platform=linux/amd64 adoptopenjdk:11-jdk-hotspot AS builder

# 소스 코드를 현재 디렉토리로 복사합니다.
#COPY --chown=gradle:gradle . /app

# 작업 디렉터리 생성
#WORKDIR /app

# Gradle Wrapper를 사용하여 프로젝트 빌드
#RUN ln -sf /usr/share/zoneinfo/Asiz/Seoul /etc/localtime
#RUN chmod +x ./gradlew
#RUN ./gradlew clean build --no-daemon -x test

# 실행 단계에서 사용할 ARM 아키텍처에 맞는 JDK 이미지
#FROM --platform=linux/amd64 adoptopenjdk:11-jdk-hotspot

#ARG JAR_FILE=build/libs/*.jar
#EXPOSE 8080

#WORKDIR /app
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java", "-jar", "/app.jar"]