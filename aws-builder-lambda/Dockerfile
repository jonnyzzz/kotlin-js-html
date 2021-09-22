FROM public.ecr.aws/lambda/provided:al2

RUN yum update -y

RUN curl -sL https://rpm.nodesource.com/setup_16.x | bash - && \
    yum install -y nodejs && \
    node -v && \
    npm -v  && \
    npm install --global yarn && \
    yarn -v


RUN yum install -y java-11-amazon-corretto-headless java-11-amazon-corretto && java -version

RUN yum install -y curl wget vim which zip

COPY build/runner /runner
RUN cd /runner && chmod +x ./warmup.sh && ./warmup.sh

### we may build Go binary also in the same container,
### e.g. as in https://docs.aws.amazon.com/lambda/latest/dg/go-image.html

### this one changes too often, let's keep that closer to the end
COPY build/lambda-builder /builder-runner/lambda-builder


ENTRYPOINT [ "/builder-runner/lambda-builder" ]
