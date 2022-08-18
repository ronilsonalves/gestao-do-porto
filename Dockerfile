FROM openjdk:19-jdk-alpine3.15
WORKDIR /gestao
EXPOSE 8080
COPY target/gestaoporto.jar /gestao/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]