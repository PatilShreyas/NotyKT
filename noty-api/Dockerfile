FROM gradle:7.6-jdk17 AS build

WORKDIR /app
COPY . /app/

RUN ./gradlew installDist --no-daemon

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/application/build/install/application/ /app/

EXPOSE 8080

CMD ["bin/application"]
