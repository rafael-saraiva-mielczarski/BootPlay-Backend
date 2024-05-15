# rafael-saraiva-backend
Projeto Backend - Rafael Saraiva Mielczarski

## Description

A system to manage an Album Collection

Two microservices were build:
 - app-integration-api: api that manages all the album related features, selling an album, removing an album, seeing an user collection of album and getting albums from the spotifyApi

 - app-user-api: api that manages all the user related features, from creating an user, authenticating the user from dealing with his wallet movements.

## Instructions

### 1. Clone the repo
```
git clone https://github.com/bc-fullstack-04/rafael-saraiva-backend.git
```

### 2. Go to repo
```
cd ./rafael-saraiva-backend
```

### 3. Run docker
Make sure you have it installed first on your machine
```
docker-compose -f docker-compose.yml build
docker-compose -f docker-compose.yml up
```