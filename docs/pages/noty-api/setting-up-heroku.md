# Set up Heroku for Deployment 🚀 [ARCHIVED]

> **Note:** Heroku is no longer being used for deployment as they have started charging for their services. 
> The NotyKT API has been moved to Koyeb. Please refer to the [Koyeb deployment documentation](./deployment-koyeb.md) 
> for current deployment information. 

_This API was deployed on https://noty-api.herokuapp.com._

## 🤖 Setting up Heroku CLI  

- Install the [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli) as per your system specification.

- After installation, execute command for authorizing Heroku CLI.

```bash
heroku login
```

_After successful authorization, you can proceed to set up application on the Heroku._

## ✨ Configuring Project on Heroku

- To create app, checkout to the project and launch command prompt/shell.

- Run the command to create project on Heroku

```bash
heroku create notykt
```

?> Here, _'notykt'_ is an app name, you can keep it as per your choice and it should be unique.

After project creation, you'll get URL for the application which can be accessible publicly once deployment is completed.

- Now create database for Heroku application backend. Run the command to create database on Heroku.

```bash
heroku addons:create heroku-postgresql:hobby-dev
```

- Once database is created, let's extract database credentials using command

```bash
heroku pg:credentials:url DATABASE
```

After running above command, you'll get database credentials which will be needed to set up on Heroku environment.

!> Keep Heroku database credentials safe!

## ⛓️ Setting up environment variables

As we set up environment variables locally we need to set up it for Heroku app as well. Just execute commands as following.

```bash
heroku config:set SECRET_KEY=f84795t89t329385htgriw598hi54
heroku config:set DATABASE_HOST=ec2-3-210-52-182.compute-1.amazonaws.com
heroku config:set DATABASE_PORT=5432
heroku config:set DATABASE_NAME=d9ks6apec762i9
heroku config:set DATABASE_USER=eolncfonigjlqw
heroku config:set DATABASE_PASSWORD=cdf91ee8a061496459u40950ae4e1fe0f69d0d3e6
```

?> _Replace your Heroku database credentials with the values which you retrieved in the previous step_.

Once this is done, just set up deployment configuration.
