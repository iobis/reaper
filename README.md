# reaper

Multithreaded scheduled IPT harvester, uses MongoDB.

## Docker

```
docker run -d --name=reaper-mongo -p 127.0.0.1:27017:27017 mongo
docker build -f docker/Dockerfile -t reaper .
```
