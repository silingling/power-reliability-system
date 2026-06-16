FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG MODULE
COPY ${MODULE}/target/${MODULE}-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
