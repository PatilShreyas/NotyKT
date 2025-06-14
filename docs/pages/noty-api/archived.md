# Archives

This page documents the deployment history of the NotyKT API service across different platforms.

## Deployment Timeline

### Heroku (Until November 2022)
This project was initially hosted on Heroku platform (Server + Database).
Since Heroku [announced the end of free tier](https://blog.heroku.com/next-chapter) in August 2022 and started charging for services in November 2022, the project was migrated away from Heroku.

The API was previously accessible at: `https://noty-api.herokuapp.com`

### Railway.app (November 2022 - June 2025)
After migrating from Heroku, the project was deployed on Railway.app.
However, Railway also started charging for their services in 2025, which prompted another migration.

The API was accessible at: `https://notykt-production.up.railway.app`

## Current Deployment

The NotyKT API is now deployed on [Koyeb](https://www.koyeb.com/) and is accessible at:
`https://notykt-api.shreyaspatil.dev`

For current deployment information, please refer to the [Koyeb deployment documentation](/pages/noty-api/deployment-koyeb).

## Archived Configuration

The following sections contain configuration details for platforms that are no longer in use. These are kept for historical reference:

- [(Railway) Deployment on Railway.app](/pages/noty-api/deployment-railway)
- [(Heroku) Setup Heroku](/pages/noty-api/setting-up-heroku)
- [(Heroku) Deployment configuration](/pages/noty-api/deployment-configuration)
- [(Heroku) Test Heroku Locally](/pages/noty-api/test-heroku-locally)
- [(Heroku) Setup GitHub Actions](/pages/noty-api/deployment-gh-actions.md)