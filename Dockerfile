# syntax=docker/dockerfile:1.5.2

# Copyright (C) 2020 - present Juergen Zimmermann, Hochschule Karlsruhe
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

# Aufruf:   docker buildx build --tag juergenzimmermann/kunde:2023.1.1-jammy .
#           Get-Content Dockerfile | docker run --rm --interactive hadolint/hadolint:2.12.1-beta-debian
#           docker compose up
#           docker compose exec kunde bash

# https://docs.docker.com/engine/reference/builder/#syntax
# https://github.com/moby/buildkit/blob/master/frontend/dockerfile/docs/syntax.md
# https://hub.docker.com/r/docker/dockerfile
# https://containers.gitbook.io/build-containers-the-hard-way
# https://docs.docker.com/develop/develop-images/multistage-build

# ---------------------------------------------------------------------------------------
# S t a g e :   b u i l d e r
#
#   "Eclipse Temurin" aus "Eclipse Adoptium Project" als JDK https://github.com/adoptium/containers
#   Ubuntu "Jammy Jellyfish" 22.04 LTS (Long Term Support) https://ubuntu.com/about/release-cycle https://wiki.ubuntu.com/Releases
#   Docker-Image fuer OpenJDK ist *deprecated*
#   Amazon Coretto stellt fuer non-LTS Releases nur Alpine als Betriebssystem und JDK bereit, nicht mit JRE (s.u.)
#   JAR mit eigenem Code und Dependencies z.B. Spring, Jackson
# ---------------------------------------------------------------------------------------
ARG TEMURIN_VERSION=20.0.1_9
FROM eclipse-temurin:${TEMURIN_VERSION}-jdk-jammy AS builder

# "working directory" fuer die Docker-Kommandos RUN, ENTRYPOINT, CMD, COPY und ADD
WORKDIR /source

COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

# JAR-Datei mit den Schichten ("layers") erstellen und aufbereiten bzw. entpacken
# Default-Kommando
# "here document" wie in einem Shellscipt
RUN <<EOF
./gradlew --no-configuration-cache --no-daemon --no-watch-fs bootJar
java -Djarmode=layertools -jar ./build/libs/kunde-2023.1.0.jar extract
EOF

# ---------------------------------------------------------------------------------------
# S t a g e   2
#
#   JRE statt JDK
#   Dependencies z.B. Spring, Jackson
#   Loader fuer Spring Boot
#   Eigener uebersetzter Code
# ---------------------------------------------------------------------------------------

FROM eclipse-temurin:${TEMURIN_VERSION}-jre-jammy

WORKDIR /application

# https://unix.stackexchange.com/questions/217369/clear-apt-get-list
RUN set -ex && \
    apt-get update && \
    apt-get upgrade --yes && \
    apt-get autoremove -y && \
    apt-get clean -y && \
    rm -rf /var/lib/apt/lists/*

RUN <<EOF
groupadd --gid 1000 kunde
useradd --uid 1000 --gid kunde --no-create-home kunde
EOF

COPY --from=builder --chown=kunde:kunde /source/dependencies/ ./
COPY --from=builder --chown=kunde:kunde /source/spring-boot-loader/ ./
#COPY --from=build --chown=kunde:kunde /source/snapshot-dependencies/ ./
COPY --from=builder --chown=kunde:kunde /source/application/ ./

USER kunde
EXPOSE 8080

# Basis-Kommando, das immer ausgefuehrt wird
ENTRYPOINT ["java", "--enable-preview", "org.springframework.boot.loader.JarLauncher"]
