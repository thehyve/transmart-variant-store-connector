#!/usr/bin/env bash
set -ex
export BUILDAH_FORMAT=docker
export MAVEN_VERSION="3.6.1-jdk-11"
export REPO_URL="https://repo.thehyve.nl/content/repositories/releases/"
export CONNECTOR_VERSION="0.0.2"
export JAVA_VERSION="11-jre"
export JAVACACERTDIR="/usr/local/openjdk-11/lib/security"
export PORT=9060
NEWCONTAINER=$(buildah from openjdk:${JAVA_VERSION})
SCRATCHMNT=$(buildah mount ${NEWCONTAINER})
buildah run ${NEWCONTAINER} useradd -b /opt -m -r connector
podman --cgroup-manager=cgroupfs run --rm -v ${SCRATCHMNT}:/mnt:rw --systemd=false maven:${MAVEN_VERSION} mvn dependency:get -DrepoUrl=${REPO_URL} -Dartifact=nl.thehyve:transmart-variant-store-connector:${CONNECTOR_VERSION}:jar -Dtransitive=false -Ddest=/mnt/opt/connector/transmart-variant-store-connector.jar
buildah config --env JAVACACERTDIR=${JAVACACERTDIR} ${NEWCONTAINER}
buildah run ${NEWCONTAINER} chown connector /opt/connector/transmart-variant-store-connector.jar ${JAVACACERTDIR}/cacerts ${JAVACACERTDIR}
buildah run ${NEWCONTAINER} chmod 644 ${JAVACACERTDIR}/cacerts
buildah copy --chown connector:connector ${NEWCONTAINER} 'connector-entrypoint.sh' '/opt/connector'
buildah config --label name=transmart-variant-store-connector ${NEWCONTAINER}
buildah config --env PORT=${PORT} ${NEWCONTAINER}
buildah config --workingdir /opt/connector ${NEWCONTAINER}
buildah config --port ${PORT} ${NEWCONTAINER}
buildah config --user connector ${NEWCONTAINER}
buildah config --entrypoint /opt/connector/connector-entrypoint.sh ${NEWCONTAINER}
buildah unmount ${NEWCONTAINER}
buildah commit ${NEWCONTAINER} transmart-variant-store-connector
buildah tag transmart-variant-store-connector docker.io/thehyve/transmart-variant-store-connector:latest
buildah tag transmart-variant-store-connector docker.io/thehyve/transmart-variant-store-connector:${CONNECTOR_VERSION}
