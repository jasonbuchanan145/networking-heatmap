FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
VOLUME /tmp
RUN apt-get update && apt-get -y install tshark
COPY target/ipheatmap-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]