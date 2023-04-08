FROM ubuntu
RUN mkdir /blockchain
ADD . /blockchain
RUN apt-get update && apt-get install -y openjdk-17-jdk openjdk-17-jre 
RUN cd /blockchain &&\
	./gradlew jar