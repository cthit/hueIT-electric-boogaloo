# backend

## about 

Built from a default ```create react-app```.

Use [yarn](https://yarnpkg.com/lang/en/) and not npm

## usage

There are two primary ways of testing the development build:

### using yarn:

This assumes you have yarn downloaded which can be installed using ```npm```

```
cd frontend
yarn install
yarn start
```

This will start a server on ```localhost:3000``` which can be tested in the browser. No known difference between this and production version.

### using docker:

This assumes you have docker installed, which can be installed using your package manager 

```
cd frontend
docker build -f Dockerfile -t cthit/hueit-frontend:dev .
docker run -it -p 3000:3000 cthit/hueit-frontend:dev
```
The production build can be tested in a similar way:

```
cd frontend
docker build -f Dockerfile-prod -t cthit/hueit-frontend:latest .
docker run -it -p 3000:80 cthit/hueit-frontend:latest
```
