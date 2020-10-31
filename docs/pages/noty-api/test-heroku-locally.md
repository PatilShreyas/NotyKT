# 🧪 Testing Heroku Locally

You can test Heroku setup locally to ensure everything is working configured correctly or not.

- Just create a file `.env` at the root directory of a project and add values as per your local set up.

```env
SECRET_KEY=ANY_RANDOM_SECRET_VALUE
DATABASE_PORT=5432
DATABASE_HOST=localhost
DATABASE_NAME=notykt_dev_db
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
```

- Run the command to run the Heroku application locally

```bash
heroku local web
```

Once you hit the command, it'll start the application execution as Heroku application at the port _5000_ and you'll be able to test it. It means you're on right track! 😃
