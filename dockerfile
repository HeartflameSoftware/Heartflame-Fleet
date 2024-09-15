# Stage 1
FROM cgr.dev/chainguard/jre as runtime
WORKDIR /etc/heartflame
COPY fleet-1.0-SNAPSHOT-shaded.jar ./heartflame-fleet.jar
ENTRYPOINT ["java", "-jar", "/etc/heartflame/heartflame-fleet.jar"]