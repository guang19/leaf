FROM openjdk:8u265-jre

MAINTAINER guang19<2196927727@qq.com>

RUN mkdir /leaf
ADD leaf-server.jar /leaf

WORKDIR /leaf

EXPOSE 13140
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/leaf/leaf-server.jar"]
