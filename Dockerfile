FROM adoptopenjdk:11-jdk-hotspot

RUN ln -sf /usr/share/zoneinfo/Asiz/Seoul /etc/localtime

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]