# Docker Usage

Docker provides a consistent and isolated environment for running the NotyKT API and its dependencies. This approach is recommended for development to ensure consistency across different environments.

## Prerequisites

Before you begin, make sure you have the following installed on your machine:

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Running with Docker

Follow these steps to run the NotyKT API using Docker:

1. Clone the repository
2. Navigate to the project root directory (`noty-api`)
3. Create a `.env` file in the root directory with the required environment variables:

```
SECRET_KEY=ANY_RANDOM_SECRET_VALUE

PGPORT=5432
PGHOST=db
PGDATABASE=notykt_dev_db
PGUSER=postgres
PGPASSWORD=postgres

DATABASE_DRIVER=org.postgresql.ds.PGSimpleDataSource
DATABASE_MAX_POOL_SIZE=10
```

4. Run the following command to start the application and database:

```bash
docker-compose up
```

This will start:
- PostgreSQL database on port 5432
- NotyKT API application on port 8080

You can access the API at `http://localhost:8080` once the services are up and running.

## Additional Docker Commands

Here are some useful Docker commands for managing the NotyKT API:

- To run the services in the background:
  ```bash
  docker-compose up -d
  ```

- To stop the services:
  ```bash
  docker-compose down
  ```

- To rebuild the application after making changes:
  ```bash
  docker-compose up --build
  ```

- To view logs:
  ```bash
  docker-compose logs -f
  ```