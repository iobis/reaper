FROM ubuntu:16.04

MAINTAINER Pieter Provoost <p.provoost@unesco.org>

RUN apt-get -y update
RUN apt-get -y install default-jdk

RUN mkdir /reaper
COPY ./target/reaper.jar /reaper

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/reaper/reaper.jar"]

EXPOSE 80 
