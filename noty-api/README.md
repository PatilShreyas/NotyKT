# NotyKT (API)

![Build (API)](https://github.com/PatilShreyas/NotyKT/workflows/Build%20(API)/badge.svg)
![Deploy (API)](https://github.com/PatilShreyas/NotyKT/workflows/Deploy%20(API)/badge.svg)

Noty backend _REST API_ is built with Ktor framework with PostgreSQL as database and deployed on the Heroku.

Currently this API is deployed on _`https://noty-api.herokuapp.com/notes`_. You can try it 😃.

## Features 👓

- Easy structure
- Authentication
- Automatic and easy deployment to Heroku
- Test cases

## About this Project 💡

This project has two modules as following:

- **`data`**: Data source and operations.
- **`application`**: Ktor application entry point and API routes.

## Development Setup 🖥

You will require latest stable version of JetBrains IntelliJ Idea to build and run the server application.

- Import this project in IntelliJ Idea
- Build the project.
- Set environment variables for the `:application:run` configuration as following

```
SECRET_KEY=ANY_RANDOM_SECRET

DATABASE_NAME=noty_db
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
```

Replace database credentials with your local config.

- Run command `./gradlew :application:run`.
- Hit `http://localhost:8080` and API will be live🔥.
- You can find sample HTTP requests [here](http/) and can directly send requests from IntelliJ itself.

## Built with 🛠

- [Ktor](https://ktor.io/) - Ktor is an asynchronous framework for creating microservices, web applications, and more. It’s fun, free, and open source.
- [Exposed](https://github.com/JetBrains/Exposed) - An ORM/SQL framework for Kotlin.
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) - JDBC Database driver for PostgreSQL.
- [Testcontainer](https://www.testcontainers.org/) - Testcontainers is a Java library that supports JUnit tests, providing lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container.
- [Kotest](https://kotest.io/) - Kotest is a flexible and comprehensive testing project for Kotlin with multiplatform support.
