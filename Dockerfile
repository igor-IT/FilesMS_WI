FROM openjdk:18 AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean install

FROM openjdk:18
WORKDIR /app
COPY --from=build /app/target/UploadMS-2307.0.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]