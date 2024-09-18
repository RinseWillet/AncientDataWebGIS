FROM openjdk:17
RUN mkdir /ancientdata
COPY ancientdata.jar /ancientdata/ancientdata.jar
WORKDIR /ancientdata

EXPOSE 8080
#COPY ./build/libs/ancientdata-0.0.1-SNAPSHOT.jar ancientdata.jar
ENTRYPOINT ["java","-jar","ancientdata.jar"]