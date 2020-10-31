# Noty REST API

- This is a backend REST API developed using [Ktor](https://ktor.io) framework.

- It uses PostgreSQL as database to store and retrieve data.

- It's deployed on [Heroku](https://heroku.com) to publicly expose API.

## 👓 Features of Codebase

- Simple structure

- JWT Authentication

- Automatic deployment to Heroku using GitHub Actions CI.

- Tests

## 📙  Overview of Codebase

This is Gradle based multi-module project which is basically a server application. 

It include two modules:

### Data

The data module consist all data related operations which interacts with data layer of the application i.e. PostgreSQL. It include database Entities and Tables of [JetBrains Exposed framework](https://github.com/JetBrains/Exposed) (ORM) and DAO classes for data operations.

You can take a look at package [here](https://github.com/PatilShreyas/NotyKT/tree/master/noty-api/data/src/dev/shreyaspatil/noty/data).

### Application

This is the main entry-point of the server application which is exposed to be accessed from a URL. It includes all the handling of routes, authentication, etc.

You can take a look at source [here](https://github.com/PatilShreyas/NotyKT/tree/master/noty-api/application).

[`application.conf`](https://github.com/PatilShreyas/NotyKT/blob/master/noty-api/application/resources/application.conf) file contain the information about the application like main module to run, environment configuration, port, etc.

## 🛠 Built with  

- [Ktor](https://ktor.io/) - Ktor is an asynchronous framework for creating microservices, web applications, and more. It’s fun, free, and open source.

- [Exposed](https://github.com/JetBrains/Exposed) - An ORM/SQL framework for Kotlin.

- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) - JDBC Database driver for PostgreSQL.

- [Testcontainer](https://www.testcontainers.org/) - Testcontainers is a Java library that supports JUnit tests, providing lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container.

- [Kotest](https://kotest.io/) - Kotest is a flexible and comprehensive testing project for Kotlin with multiplatform support.
