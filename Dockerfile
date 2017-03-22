FROM anapsix/alpine-java:8

MAINTAINER Pieter Provoost <p.provoost@unesco.org>

RUN mkdir /reaper
COPY ./target/reaper.jar /reaper

ENTRYPOINT ["java"]
CMD ["-jar", "/reaper/reaper.jar"]

EXPOSE 8080
