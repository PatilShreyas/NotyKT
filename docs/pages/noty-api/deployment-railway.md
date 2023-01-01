# Deployment on Railway.app

This project is currently deployed on Railway.app on the following URLs
- https://notykt-production.up.railway.app/notes
- https://notykt-api.shreyaspatil.dev

Deployment on Railway is very simple. On every push, it automatically deploys project.

![Railway.app Dashboard - NotyKT](/media/noty-api/railway-dashboard.png)

## Deployment Pre-requisites

- Create a project on Railway.app (from GitHub).
- Configure deployment branch as `master` and directory `noty-api` (_as API source is in directory_).
- Add PostgreSQL Database service in Railway.app.
- Configure environment variables (_as seen in the above image_).
- Create a Procfile with following configuration for starting up app server:

```Procfile
web: application/build/install/application/bin/application
```

- After this, just deploy and check whether everything is working fine.