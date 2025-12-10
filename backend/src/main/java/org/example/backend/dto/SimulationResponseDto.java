package org.example.backend.dto;

public class SimulationResponseDto {
    private Long simulationRunId;
    private String status;
    private String message;

    public SimulationResponseDto() {
    }

    public SimulationResponseDto(Long simulationRunId, String status, String message) {
        this.simulationRunId = simulationRunId;
        this.status = status;
        this.message = message;
    }

    public Long getSimulationRunId() {
        return simulationRunId;
    }

    public void setSimulationRunId(Long simulationRunId) {
        this.simulationRunId = simulationRunId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
