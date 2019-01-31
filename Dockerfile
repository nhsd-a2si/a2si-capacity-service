FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG VERSION
ARG PROFILE

ADD ./target/a2si-capacity-service-${VERSION}.jar a2si-capacity-service.jar
ADD ./keystore.jks keystore.jks

# Expose 7020, the default port used for Capacity Service
EXPOSE 7020
ENV JAVA_OPTS=""
RUN ["apk", "update"]
RUN ["apk", "add", "tzdata"]
RUN ["ln", "-f", "-s", "/usr/share/zoneinfo/Europe/London", "/etc/localtime"]
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar -Dspring.profiles.active=$PROFILE a2si-capacity-service.jar" ]
