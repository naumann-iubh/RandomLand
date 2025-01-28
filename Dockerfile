FROM eclipse-temurin:21

RUN mkdir /app
WORKDIR /app

# Copy the build artifacts from the build stage
COPY target/quarkus-app/lib/ /app/lib/
COPY target/quarkus-app/*.jar /app/
COPY target/quarkus-app/app/ /app/app/
COPY target/quarkus-app/quarkus/ /app/quarkus/
COPY application.properties /app/

ENV JAVA_OPTS_APPEND="-Dquarkus.config.locations=/app/application.properties -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

EXPOSE 8080


# Set the entrypoint and command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/quarkus-run.jar"]