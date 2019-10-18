# Backend

## About 

Built using Kotlin and KTOR because we felt it was sufficiently off flavor, but still resembling Java to the point where new IT-students would find it approachable.

## Starting

There are two primary ways of starting the development build:

### Using gradle:

This assumes you have gradle version 5.0.0 or higher installed.

```
cd backend
gradle build
gradle shadowJar
gradle runShadow --args <insert hue API-key here>
```

### Using docker:

This assumes you have docker installed, which can be installed using your package manager 

```
cd backend
docker build . -t cthit/hueit-frontend:dev .
docker run -p 8080:8080 cthit/hueit-frontend:dev <insert hue API-key here>
```

The production build can be tested in a similar way:

```
cd frontend
docker build -t cthit/hueit-backend:latest .
docker run -p 8080:8080 cthit/hueit-backend:latest <insert hue API-key here>
```

## Usage

There are two primary ways of using the development build:

### Using the frontend:

Look at the readme in the frontend-folder for more info.

### Using some HTTP-tester:

This assumes that you have something like curl or Insomnia installed.

By default the backend is opened on localhost:8080.

At the moment the HueIT-backend only accepts POST-requests and only to two routes: '/' if only affecting one lamp or group, and '/list' if affecting more than one lamp or group. The only difference in what JSON-objects they receive is that '/' accepts a singular so called 'RequestBody', while '/list' accepts JSON-object with one field called 'requestBodyList' which holds an array of RequestBodies.

A RequestBody consists of the following fields:
  - **isGroup**: a boolean that decides whether a groupof lamps or a single lamp is accessed.
  - **id**: an integer that determines what lamp or group is accessed, with the following legal values:
    - isGroup == true: 1 and 2, functionally equivalent at the moment
    - isGroup == false: 0-7
  - **props**: another JSON that determines what states are changed and what they are changed to, this JSON has the following fields:
    - **pwr**: a boolean, false == off, true == on.
    - **hue**: a double, the hue of the light in degrees.
    - **sat**: a double, the saturation of the light from 0 to 1.
    - **bri**: a double, the brightness of the light from 0 to 1.
    - **rst**: a boolean, resets the light/group of lights to the default value.
    
#### Example of usage:

Send the following JSON-object to localhost:8080/ to turn on lamp 2 with a nice turquoise colour:
```
{
	"isGroup": false,
	"id": 2,
	"props": {
		"pwr": true,
		"hue": 181,
		"sat": 75,
		"bri": 100
	}
}
```
