FROM openjdk:17

RUN mkdir /app

COPY /build/libs/ancientdata-0.0.1-SNAPSHOT.jar /app/ancientdata.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/ancientdata.jar"]