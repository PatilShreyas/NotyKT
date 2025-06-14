# NotyKT (API)

![Build (API)](https://github.com/PatilShreyas/NotyKT/workflows/Build%20(API)/badge.svg)

Noty backend _REST API_ is built with Ktor framework with PostgreSQL as database and deployed on the [Railway](https://railway.app).

Currently this API is deployed on _`https://notykt-production.up.railway.app`. You can try it 😃.

[📄 _**Visit the documentation of this project**_](https://patilshreyas.github.io/NotyKT/) to get more information in detail.

## Features 👓

- Easy structure
- Authentication
- Automatic and easy deployment to Railway.app
- Test cases

## About this Project 💡

This project has two modules as following:

- **`data`**: Data source and operations.
- **`application`**: Ktor application entry point and API routes.

## Development Setup 🖥

### Using IntelliJ IDEA

You will require latest stable version of JetBrains IntelliJ Idea to build and run the server application.

- Import this project in IntelliJ Idea
- Build the project.
- Set environment variables for the `:application:run` configuration as following

_Refer to the [`.env`](.env) file for example of environment variables._

```
SECRET_KEY=ANY_RANDOM_SECRET_VALUE

PGPORT=5432
PGHOST=localhost
PGDATABASE=notykt_dev_db
PGUSER=postgres
PGPASSWORD=postgres

DATABASE_DRIVER=org.postgresql.ds.PGSimpleDataSource
DATABASE_MAX_POOL_SIZE=10
```

Replace database credentials with your local config.

- Run command `./gradlew :application:run`.
- Hit `http://localhost:8080` and API will be live🔥.
- You can find sample HTTP requests [here](http/) and can directly send requests from IntelliJ itself.

### Using Docker

You can also use Docker to run the application and its dependencies. This is the recommended approach for a consistent 
development environment.

#### Prerequisites

- [Docker](https://www.docker.com/get-started) installed on your machine
- [Docker Compose](https://docs.docker.com/compose/install/) installed on your machine

#### Steps to run with Docker

1. Clone the repository
2. Navigate to the project root directory
3. Create a `.env` file in the root directory with the required environment variables (see example above)
4. Run the following command to start the application and database:

```bash
docker-compose up
```

This will start:
- PostgreSQL database on port 5432
- NotyKT API application on port 8080

To run the services in the background, use:

```bash
docker-compose up -d
```

To stop the services:

```bash
docker-compose down
```

To rebuild the application after making changes:

```bash
docker-compose up --build
```

You can access the API at `http://localhost:8080` once the services are up and running.

## Built with 🛠

- [Ktor](https://ktor.io/) - Ktor is an asynchronous framework for creating microservices, web applications, and more. It’s fun, free, and open source.
- [Exposed](https://github.com/JetBrains/Exposed) - An ORM/SQL framework for Kotlin.
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) - JDBC Database driver for PostgreSQL.
- [Testcontainer](https://www.testcontainers.org/) - Testcontainers is a Java library that supports JUnit tests, providing lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container.
- [Kotest](https://kotest.io/) - Kotest is a flexible and comprehensive testing project for Kotlin with multiplatform support.

# REST API Specification

You can navigate to [`/http`](http/) and try API calls in IntelliJ Idea IDE itself after API is running.

## Authentication

### Register

```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "test12345",
    "password": "12346789"
}

```

### Login

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "test12345",
    "password": "12346789"
}

```

## Note Operations

### Get all Notes

```http
GET http://localhost:8080/notes
Content-Type: application/json
Authorization: Bearer YOUR_AUTH_TOKEN
```

### Create New Note

```http
POST http://localhost:8080/note/new
Content-Type: application/json
Authorization: Bearer YOUR_AUTH_TOKEN

{
  "title": "Hey there! This is title",
  "note": "Write note here..."
}
```

### Update Note

```http
PUT http://localhost:8080/note/NOTE_ID_HERE
Content-Type: application/json
Authorization: Bearer YOUR_AUTH_TOKEN

{
  "title": "Updated title!",
  "note": "Updated body here..."
}
```

### Delete Note

```http
DELETE http://localhost:8080/note/NOTE_ID_HERE
Content-Type: application/json
Authorization: Bearer YOUR_AUTH_TOKEN
```
