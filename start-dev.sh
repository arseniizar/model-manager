#!/bin/bash
set -e

echo "--- 1. Starting Minikube ---"
if ! minikube status &> /dev/null; then
    echo "Minikube is not running. Starting..."
    minikube start --cpus=4 --memory=4096
else
    echo "Minikube is already running."
fi

echo "--- 2. Pointing Docker CLI to Minikube's Docker daemon ---"
eval $(minikube -p minikube docker-env)

echo "--- 3. Building project and Docker image inside Minikube ---"
docker build -t arseniizar/model-manager-backend:latest -f backend/Dockerfile .

echo "--- 4. Deploying resources to Kubernetes ---"
kubectl apply -f k8s/

echo "--- 5. Waiting for all deployments to become available ---"
echo "Waiting for PostgreSQL..."
kubectl wait --for=condition=available --timeout=5m deployment/postgres-deployment
echo "Waiting for Kafka..."
kubectl wait --for=condition=available --timeout=5m deployment/kafka-deployment
echo "Waiting for Backend..."
kubectl wait --for=condition=available --timeout=5m deployment/backend-deployment
echo "All systems are GO!"

echo "--- 6. Starting port-forward to backend service ---"
kubectl port-forward service/backend-service 8080:8080 > /tmp/port-forward.log 2>&1 &
PORT_FORWARD_PID=$!

cleanup() {
    echo -e "\n--- Cleaning up ---"
    echo "Stopping port-forward (PID: $PORT_FORWARD_PID)..."
    kill $PORT_FORWARD_PID || true
    echo "To delete all resources from Kubernetes, run: kubectl delete -f k8s/"
    eval $(minikube -p minikube docker-env -u)
}
trap cleanup EXIT

echo "--- 7. Verifying backend is accessible via port-forward ---"
for i in {1..60}; do
    if curl --output /dev/null --silent --head --fail http://localhost:8080/actuator/health; then
        echo -e "\nBackend is up and running on localhost:8080!"
        break
    fi
    printf '.'
    sleep 1
done

if ! curl --output /dev/null --silent --head --fail http://localhost:8080/actuator/health; then
    echo -e "\nERROR: Backend did not become available after 60 seconds. Check logs:"
    kubectl logs deployment/backend-deployment
    exit 1
fi

UI_JAR=$(find ui-swing/target -name "ui-swing-*.jar" | head -n 1)
if [ -z "$UI_JAR" ]; then
    echo "ERROR: Could not find UI JAR file."
    exit 1
fi

echo "--- Starting UI application ---"
java -Dbackend.api.url=http://localhost:8080/api/simulations -jar "$UI_JAR"

echo "--- UI closed. Script finished. ---"
