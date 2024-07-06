FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder

ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY . $HOME
ENV PROFILE=prod
RUN --mount=type=cache,target=/home/adnan/.m2 mvn -f $HOME/pom.xml clean install
RUN mvn package spring-boot:repackage

FROM eclipse-temurin:21
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Dhaka /etc/localtime \
&& echo "Asia/Dhaka" > /etc/timezone

WORKDIR /opt/app

#ENV CONFIG_URI=configserver:http://192.168.1.137:30010/
#ENV CONFIG_URI=configserver:http://172.16.5.10:30010/

EXPOSE 8887

COPY --from=builder /usr/app/target/*.jar /opt/app/app.jar

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar" ]