#!/bin/bash

echo "--- Starting Docker container for the database ---"
docker-compose up -d

echo "--- Building the project with Maven ---"
./mvnw clean install -DskipTests

echo "--- Starting Backend and UI applications ---"
java -jar backend/target/backend-*.jar &
BACKEND_PID=$!

java -jar ui-swing/target/ui-swing-*.jar

echo "--- UI closed, stopping backend ---"
kill $BACKEND_PID