ARG SOURCE_IMAGE_TAG
FROM artefakt.dev.sbb.berlin:5000/sbb/base-images/java21-base:$SOURCE_IMAGE_TAG

# avoid debconf and initrd
ENV DEBIAN_FRONTEND=noninteractive

ENV SERVICE_NAME=hsp-fo-indexer

COPY --chown=251:251 ./target/${SERVICE_NAME}*.war /app/${SERVICE_NAME}.war

WORKDIR /app

USER 251:251

ENTRYPOINT ["java", "-jar", "/app/hsp-fo-indexer.war"]