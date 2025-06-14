# Deployment on Koyeb

The NotyKT API is now deployed on Koyeb, as Railway has started charging for their services. 

The API is live and accessible at:

- https://notykt-api.shreyaspatil.dev

## About Koyeb

[Koyeb](https://www.koyeb.com/) is a developer-friendly serverless platform that allows you to deploy applications globally with simple 
Git-based workflows. It provides automatic HTTPS, global load balancing, and autoscaling capabilities.

## Deployment Configuration

The NotyKT API deployment on Koyeb includes:
- The API service
- PostgreSQL database

## Environment Variables

Make sure to configure the following environment variables in your Koyeb deployment:

```
SECRET_KEY=YOUR_SECRET_KEY

# Database configuration
DATABASE_DRIVER=org.postgresql.ds.PGSimpleDataSource
DATABASE_MAX_POOL_SIZE=10

# PostgreSQL connection details will be provided by Koyeb if using their managed database
PGHOST=your_db_host
PGPORT=5432
PGDATABASE=your_db_name
PGUSER=your_db_user
PGPASSWORD=your_db_password
```

## Deployment Process

1. Create an account on [Koyeb](https://www.koyeb.com/)
2. Connect your GitHub repository
3. Create a new Database service in Koyeb:
   - Choose PostgreSQL
   - Set the database name, user, and password
   - Koyeb will provide the connection details which you will use in the environment variables
4. Configure the deployment settings:
   - Select the repository and branch
   - Set the build directory to `noty-api`
   - Configure environment variables (also update the database connection details)
5. Deploy the application

## Accessing the API

Once deployed, your API will be accessible at the URL provided by Koyeb. 

You can use this URL for all API endpoints as described in the [API documentation](getting-started.md).
