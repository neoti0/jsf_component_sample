# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -q

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre

# OS ロケールを UTF-8 に統一
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8
ENV LANGUAGE=en_US:en

# JVM の文字コードを UTF-8 に固定（JAVA_TOOL_OPTIONS は JVM 起動時に常に適用される）
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8"

ENV WILDFLY_VERSION=31.0.0.Final
ENV WILDFLY_HOME=/opt/wildfly

RUN apt-get update && apt-get install -y curl locales && \
    locale-gen en_US.UTF-8 && \
    curl -fsSL https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz \
    | tar -xz -C /opt && \
    mv /opt/wildfly-${WILDFLY_VERSION} ${WILDFLY_HOME} && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/target/jsf-sample.war ${WILDFLY_HOME}/standalone/deployments/

EXPOSE 8080 9990

CMD ["/opt/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
