# reaper

Multithreaded scheduled IPT harvester, uses MongoDB.

## Docker

```
# start mongo container
docker run -d --name=reaper-mongo -p 127.0.0.1:27017:27017 mongo

# build reaper image
docker build -f docker/Dockerfile -t reaper .

# start reaper container
docker run -p 80:80 --link reaper-mongo:reaper-mongo -it reaper
```
