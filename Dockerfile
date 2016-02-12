FROM ubuntu:14.04

RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
RUN echo "deb http://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
RUN apt-get update
RUN apt-get install -yqq openjdk-7-jdk golang erlang erlang-doc ghc ghc-prof ghc-doc scala sbt leiningen time git cabal-install curl build-essential checkinstall libgflags-dev musl
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY models models/
COPY scripts scripts/
COPY musl musl/

ENV GOPATH $HOME/go
ENV PATH $PATH:$GOROOT/bin:$GOPATH/bin
ENV LEIN_ROOT 1

WORKDIR /scripts/

CMD ["bash"]

#CMD /
#CMD go build zi-traders.go
#CMD echo "Built Go model"
#CMD bash -c "/usr/bin/time -f '1,%e,%U,%S' ./zi-traders"

#docker-machine create -d virtualbox --virtualbox-boot2docker-url file://$HOME/Dropbox/boot2docker-v1.9.1-fix1.iso --virtualbox-memory 1536 --virtualbox-disk-size 10000 fixedjava
#docker-machine create -d virtualbox --virtualbox-boot2docker-url https://github.com/tianon/boot2docker-legacy/releases/download/v1.10.0-rc1/boot2docker.iso --virtualbox-memory 1536 --virtualbox-disk-size 10000 --virtualbox-cpu-count 2 fixedjava
