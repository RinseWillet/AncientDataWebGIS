FROM openjdk:17
RUN mkdir /ancientdata
COPY ancientdata.jar /ancientdata/ancientdata.jar
WORKDIR /ancientdata

EXPOSE 8080

ENTRYPOINT ["java","-jar","ancientdata.jar"]

#old code line for local building
#COPY ./build/libs/ancientdata-0.0.1-SNAPSHOT.jar ancientdata.jar