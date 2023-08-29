FROM amazoncorretto:11-alpine-jdk
COPY target/*jar gate.jar
ENTRYPOINT ["java","-jar","/gate.jar"]