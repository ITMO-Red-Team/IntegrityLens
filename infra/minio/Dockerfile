FROM golang:1.24.8-alpine AS build
WORKDIR /source
RUN wget -q https://codeload.github.com/minio/minio/tar.gz/refs/tags/RELEASE.2025-10-15T17-29-55Z -O minio.tar.gz \
    && echo 'be6d0bd3696c3a13a35f02d3a0280b64319c67918b4501c5c3d87f96d000085c  minio.tar.gz' | sha256sum -c - \
    && tar -xzf minio.tar.gz --strip-components=1
RUN --mount=type=cache,target=/go/pkg/mod --mount=type=cache,target=/root/.cache/go-build \
    CGO_ENABLED=0 GOMAXPROCS=2 go build -p 2 -trimpath -ldflags='-s -w' -o /out/minio .

FROM alpine:3.22
RUN apk add --no-cache ca-certificates && adduser -D -u 10001 app && mkdir /data && chown app /data
COPY --from=build /out/minio /usr/local/bin/minio
COPY --from=build /source/LICENSE /usr/share/licenses/minio/LICENSE
USER app
EXPOSE 9000 9001
ENTRYPOINT ["minio"]
CMD ["server", "/data", "--console-address", ":9001"]
