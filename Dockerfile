FROM openjdk:17

RUN mkdir /app
WORKDIR /app
CMD ["./gradlew", "clean", "bootJar"]
COPY ./build/libs/*.jar ancientdata.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ancientdata.jar"]