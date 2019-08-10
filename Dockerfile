FROM openjdk:8u151-jdk-alpine

ENV SCALA_VERSION=2.12.9 \
    SCALA_HOME=/usr/share/scala \
    SBT_VERSION=1.2.8

# NOTE:  install scala
RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
    apk add --no-cache bash curl jq && \
    cd "/tmp" && \
    wget --no-verbose "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
    tar xzf "scala-${SCALA_VERSION}.tgz" && \
    mkdir "${SCALA_HOME}" && \
    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
    apk del .build-dependencies && \
    rm -rf "/tmp/"*

# Install sbt
RUN mkdir -p "/usr/local/sbt" && \
  wget -qO - --no-check-certificate "https://piccolo.link/sbt-${SBT_VERSION}.tgz" | \
  tar xz -C /usr/local/sbt --strip-components=1 && \
  export PATH="/usr/local/sbt/bin:$PATH" && sbt sbtVersion

# Install git
RUN apk add --no-cache git

# Install docker
RUN apk --no-cache add docker

# Install jekyll
RUN apk upgrade --update \
 && apk add ruby ruby-dev  build-base ruby-json \
 && gem install --no-document jekyll -v 2.5 \
 && rm -rf /root/src /tmp/* /usr/share/man /var/cache/apk/*

RUN apk del build-base

RUN ln -s /usr/local/sbt/bin/sbt /bin/sbt
COPY . /sbt-regression-suite
RUN mv /sbt-regression-suite/sampletest /sbt-regression-suite/src/test/scala/org/vaslabs/sampletest
WORKDIR /sbt-regression-suite
