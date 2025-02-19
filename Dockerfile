ARG SOURCE_IMAGE_TAG
FROM artefakt.dev.sbb.berlin:5000/sbb/base-images/java-base:$SOURCE_IMAGE_TAG

# avoid debconf and initrd
ENV DEBIAN_FRONTEND noninteractive
#ENV INITRD No

ENV SERVICE_NAME=hsp-fo-indexer

ARG ARTEFAKT_VERSION
COPY ./target/${SERVICE_NAME}-$ARTEFAKT_VERSION.war /app/${SERVICE_NAME}.war
RUN mkdir -p /data/log/${SERVICE_NAME} && \
    mkdir -p /etc/SBB/${SERVICE_NAME} && \
    mkdir -p /usr/local/SBB/usr/local/${SERVICE_NAME} && \
    mkdir -p /etc/letsencrypt/renewal-hooks/post/${SERVICE_NAME}-letsEncrypt.post;
WORKDIR /app

RUN addgroup --system ${SERVICE_NAME} && adduser --system --shell /bin/false --ingroup ${SERVICE_NAME} ${SERVICE_NAME}
RUN chown -R ${SERVICE_NAME}:${SERVICE_NAME} /app && \
    chown -R ${SERVICE_NAME}:${SERVICE_NAME} /data/log/${SERVICE_NAME} && \
    chown -R ${SERVICE_NAME}:${SERVICE_NAME} /etc/SBB/${SERVICE_NAME} && \
    chown -R ${SERVICE_NAME}:${SERVICE_NAME} /usr/local/SBB/usr/local/${SERVICE_NAME} && \
    chown ${SERVICE_NAME}:${SERVICE_NAME} /etc/letsencrypt/renewal-hooks/post/${SERVICE_NAME}-letsEncrypt.post;
USER ${SERVICE_NAME}
ENTRYPOINT ["java", "-jar", "/app/${SERVICE_NAME}.war"]
