FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive
ENV LC_ALL=C.UTF-8

RUN apt-get update && \
    apt-get install -y apt-utils sudo curl wget mc vim git software-properties-common

RUN curl -sL https://deb.nodesource.com/setup_16.x | bash - && \
    apt-get update && \
    apt-get install -y nodejs && \
    node -v && \
    npm -v  && \
    npm install --global yarn && \
    yarn -v

RUN wget -O- https://apt.corretto.aws/corretto.key | sudo apt-key add - && \
    add-apt-repository 'deb https://apt.corretto.aws stable main' && \
    apt-get update && sudo apt-get install -y java-11-amazon-corretto-jdk

RUN mkdir -p /runner

COPY build/runner /runner
RUN chmod a+x /runner/warmup.sh

### this line adds ~750mb of data to the image
RUN /runner/warmup.sh

COPY build/lambda-builder /builder-runner/lambda-builder
RUN chmod +x /builder-runner/lambda-builder

ENTRYPOINT [ "/builder-runner/lambda-builder" ]
