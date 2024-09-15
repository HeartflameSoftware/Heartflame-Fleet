# Stage 1
FROM cgr.dev/chainguard/jre as runtime
WORKDIR /etc/heartflame
COPY heartflame-fleet.jar ./heartflame-fleet.jar
ENTRYPOINT ["java", "-jar", "/etc/heartflame/heartflame-fleet.jar"]