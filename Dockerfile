FROM openjdk:8-jdk-alpine
VOLUME /tmp

ADD ./target/a2si-capacity-service-0.2.0.jar a2si-capacity-service.jar
ADD ./keystore.jks keystore.jks

# Expose 7020, the default port used for Capacity Service
EXPOSE 7020
ENV JAVA_OPTS=""
RUN ["apk", "update"]
RUN ["apk", "add", "tzdata"]
RUN ["ln", "-f", "-s", "/usr/share/zoneinfo/Europe/London", "/etc/localtime"]
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar a2si-capacity-service.jar" ]
