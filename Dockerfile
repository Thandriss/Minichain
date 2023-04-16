FROM gradle:7.5.1-jdk17 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN ["gradle", "jar"]

FROM openjdk:17-oracle
WORKDIR /minichain
COPY --from=builder /builder/build/libs/Minichain-0.0.1.jar .