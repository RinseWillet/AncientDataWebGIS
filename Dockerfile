FROM openjdk:17

RUN mkdir /ancientdata

COPY ancientdata.jar /ancientdata/ancientdata.jar

WORKDIR /ancientdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ancientdata.jar"]