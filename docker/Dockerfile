# Build like this:
#     docker build -t <version> -f Dockerfile ..
# e.g.
#     docker build -t glarad/niord-uk:latest -f Dockerfile ..
#
# Run like this:
#     sudo docker run -t -i --rm -p 8888:8888 -v /path/to/config-directory/on/machine:/conf <image-id>
#
# You can also push to docker-hub like:
#     docker push glarad/niord-uk:tagname
#

# Use the official OpenJDK 17 image as the base image
FROM openjdk:17-jdk-slim

LABEL org.opencontainers.image.source="https://github.com/gla-rad/niord-uk"

# Install some dependencies
RUN apt-get update && apt-get install -y libpng-dev
RUN apt-get update && apt-get install -y libfreetype6-dev

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY niord-uk-web/target/quarkus-app/ .

# Create a modules path just in case
ENV LOADER_PATH /modules
RUN mkdir $LOADER_PATH

EXPOSE 8888

CMD ["java", "-jar", "quarkus-run.jar"]
