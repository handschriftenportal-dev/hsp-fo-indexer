#
# Build stage
#
FROM maven:3.6.3-openjdk-11 AS build
ARG SOLR_HOST
# Build an Empty WAR File just to download all the maven dependencies in Docker Build Cache, that the second build is 4 minutes faster,
# because the dependencies are cache in the docker build cache.
# At this point the build is done if a pom.xml change happens and at the first time
COPY mvnsettings.xml /root/.m2/settings.xml
COPY pom.xml /
COPY ./src/main/resources /src/main/resources/
COPY ./src/main/java/staatsbibliothekberlin/hsp/fo/indexer/HspFoIndexerApplication.java src/main/java/staatsbibliothekberlin/hsp/fo/indexer/HspFoIndexerApplication.java
RUN mvn clean package -Dmaven.test.skip=true
# At this point the traget folder is deletes and the build of this layer is done if a code change happens.
COPY src /src/
RUN mvn clean package -Dspring.profiles.active=dev -Dsolr.host=${SOLR_HOST}
#
# Package stage
#
# openjdk:11-jdk-alpine gibt es nicht, alpine wird aus dem Grund der Image größen verwendet.
FROM openjdk:13-jdk-alpine
COPY --from=build ./target/hsp-fo-indexer*.war /hsp-fo-indexer.war
EXPOSE 9300
RUN apk update && apk add --no-cache gcompat
ENTRYPOINT ["java", "-jar", "/hsp-fo-indexer.war"]
