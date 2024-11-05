# Base image with Java 11, SBT 1.6.2, and Scala 2.12.15 pre-installed
FROM sbtscala/scala-sbt:eclipse-temurin-11.0.14.1_1.6.2_2.12.15

# Set the working directory
WORKDIR /app

# Copy project files into the container
COPY project/ ./project/
COPY build.sbt ./
COPY src/ ./src/

# Run SBT tests
CMD ["sbt", "test"]
