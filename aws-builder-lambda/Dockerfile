FROM public.ecr.aws/lambda/provided:al2


### we may build Go binary also in the same container,
### e.g. as in https://docs.aws.amazon.com/lambda/latest/dg/go-image.html

### this one changes too often, let's keep that closer to the end
COPY build/lambda-builder /builder-runner/lambda-builder


ENTRYPOINT [ "/builder-runner/lambda-builder" ]
