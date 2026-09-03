FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
ENV TZ=Asia/Seoul
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]