FROM openjdk:17

RUN mkdir /ancientdata

COPY ./build/libs/ancientdata-0.0.1-SNAPSHOT.jar ancientdata.jar

WORKDIR /ancientdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ancientdata.jar"]