FROM gradle:8.14.1-jdk17 AS build

WORKDIR /app

# Copy only the gradle files first to leverage Docker cache
COPY gradle/ gradle/
COPY gradlew gradlew.bat settings.gradle.kts ./
COPY build.gradle.kts ./
COPY application/build.gradle.kts application/
COPY data/build.gradle.kts data/

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the application
COPY . .

# Build the application
RUN ./gradlew installDist --no-daemon

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/application/build/install/application/ /app/

# Create a non-root user to run the application
RUN groupadd -r notykt && useradd -r -g notykt notykt
RUN chown -R notykt:notykt /app
USER notykt

EXPOSE 8080

CMD ["bin/application"]
