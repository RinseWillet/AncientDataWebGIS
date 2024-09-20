FROM openjdk:17

COPY ./build/libs/ancientdata-0.0.1-SNAPSHOT.jar ancientdata.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ancientdata.jar"]