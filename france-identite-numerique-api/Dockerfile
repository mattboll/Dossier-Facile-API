ARG REGISTRY
FROM $REGISTRY/prod/fr/sgin/common/java/openjdk11
ARG NEXUS_URL
ARG NEXUS_USER_CI
ARG NEXUS_USER_CI_PASSWORD

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ARG OPENTELEMETRY_VERSION=1.23.1
ARG OPENTELEMETRY_SHA256=3980d3acbfa253e81236e353d0e26bdcfc243a59c0d0f3caa22ca483d1a84e9e
RUN curl -k -u $NEXUS_USER_CI:$NEXUS_USER_CI_PASSWORD $NEXUS_URL/maven-releases/fr/minint/sgin/opentelemetry/${OPENTELEMETRY_VERSION}/opentelemetry-${OPENTELEMETRY_VERSION}.jar -o agent-otel.jar
RUN echo "$OPENTELEMETRY_SHA256  agent-otel.jar" | sha256sum -c -

ENTRYPOINT exec java -javaagent:/agent-otel.jar -Dotel.traces.exporter=none -Dotel.metrics.exporter=none -jar /app.jar
