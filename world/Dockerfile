FROM adoptopenjdk/openjdk11:jre-11.0.3_7-alpine

COPY ./build/libs/*-all.jar /opt/app.jar

ENTRYPOINT ["java", "-jar", "/opt/app.jar"]

