# Model Simulator & Scripting Platform

Java platform for running dynamic simulations, featuring a Spring Boot backend with a TimescaleDB/PostgreSQL database and Apache Kafka, core simulation engine, and Swing-based desktop UI for scripting and data visualization.

## Screenshots

![Application Screenshot](./assets/1.png)
![Application Screenshot](./assets/2.png)
![Application Screenshot](./assets/3.png)

## Functionality

- **Multi-Module Architecture**: The project uses a Maven multi-module setup (`backend`, `simulation-core`, `ui-swing`, `simulation-api`) for separation of concerns.
- **REST Backend**: The Spring Boot backend provides a REST API for asynchronous simulation execution and results retrieval.
- **Dockerized Database**: Includes a `docker-compose.yml` for a TimescaleDB instance, suitable for time-series simulation data.
- **Groovy Scripting**: The Swing GUI integrates an `RSyntaxTextArea` editor to execute Groovy scripts against model parameters.
- **Data Integration**: The UI can load input data from local text files and display results from previous simulations fetched via the backend API.
- **Desktop UI**: The client is built with Java Swing and uses the FlatLaf Dark theme.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3, Spring Data JPA, Hibernate, PostgreSQL driver, Spring Kafka.
- **Database**: Dockerized TimescaleDB / PostgreSQL.
- **GUI**: Java Swing, FlatLaf, RSyntaxTextArea.
- **API Client**: OkHttp3, Jackson.
- **Core/Scripting**: Groovy.
- **Build**: Apache Maven.

## Project Structure

- **`simulation-api`**: DTOs for client-server communication.
- **`simulation-core`**: Abstract simulation logic, model definitions (`@Bind` annotation), and the Groovy script execution controller.
- **`backend`**: Spring Boot application exposing the simulation core via a REST API.
- **`ui-swing`**: The desktop GUI for user interaction.

## Getting Started

### Prerequisites
- Java (JDK) 17 or later.
- Apache Maven 3.8+
- Docker and Docker Compose.
- An IDE like IntelliJ IDEA (recommended).

### How to Run

#### 1. Start Infrastructure
Launch the TimescaleDB and Kafka containers using Docker Compose. In the project root, run:
```sh
docker-compose up -d
```
This will start a database instance accessible on `localhost:5432` and a Kafka broker on `localhost:9092`.

#### 2. Run the Application
You can run the backend and frontend separately.

**Option A: Using IntelliJ IDEA (Recommended)**
The project includes pre-configured run configurations.
1.  **Start the Backend**: Select the `BackendApplication` run configuration and run.
2.  **Start the GUI Client**: Once the backend is running, select the `UI` run configuration and run.

**Option B: Manual Execution (Command Line)**
1.  **Build the project**:
    ```sh
    mvn clean install
    ```
2.  **Run the Backend** (in one terminal):
    ```sh
    cd backend
    mvn spring-boot:run
    ```
3.  **Run the GUI Client** (in a new terminal):
    ```sh
    cd ui-swing
    mvn exec:java
    ```

**Option C: Using the Development Script**
The project includes a convenience script `start-dev.sh` to launch the entire development environment (database, backend, and GUI).

1.  **Make the script executable** (only needs to be done once):
    ```sh
    chmod +x start-dev.sh
    ```
2.  **Run the script**:
    ```sh
    ./start-dev.sh
    ```
This will handle starting the Docker container, building the project, and launching both the backend server and the UI client.

## API Endpoints

- `POST /api/simulations/run`: Submits a new simulation for execution.
- `GET /api/simulations/runs`: Returns a list of all completed simulation runs.
- `GET /api/simulations/{runId}/results`: Returns the detailed results for a specific simulation run.
