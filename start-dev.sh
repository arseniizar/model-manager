#!/bin/bash

set -e

echo "--- Starting Docker container for the database ---"
docker-compose up -d

if [ ! -f "mvnw" ]; then
    echo "Maven Wrapper not found. Please run 'mvn -N io.takari:maven:wrapper' first."
    exit 1
fi

echo "--- Building the project with Maven ---"
./mvnw clean install -DskipTests

echo "--- Starting Backend and UI applications ---"

BACKEND_JAR=$(find backend/target -name "backend-*.jar" | head -n 1)
UI_JAR=$(find ui-swing/target -name "ui-swing-*.jar" | head -n 1)

if [ -z "$BACKEND_JAR" ] || [ -z "$UI_JAR" ]; then
    echo "Could not find JAR files in target directories. Build may have failed."
    exit 1
fi

echo "Found backend JAR: $BACKEND_JAR"
echo "Found UI JAR: $UI_JAR"

java -jar "$BACKEND_JAR" &
BACKEND_PID=$!

echo "Waiting for backend to be available on port 8080..."
while ! nc -z localhost 8080; do
  sleep 0.5
  echo -n "."
done
echo -e "\nBackend is up and running!"

java -jar "$UI_JAR"

echo "--- UI closed, stopping backend ---"
kill $BACKEND_PID

echo "--- Script finished ---"
