package simulation.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationRunDto {
    private Long id;
    private String modelName;
    private LocalDateTime startTime;
    private String status;

    public SimulationRunDto(Long id, String modelName, LocalDateTime startTime, String status) {
        this.id = id;
        this.modelName = modelName;
        this.startTime = startTime;
        this.status = status;
    }

    public SimulationRunDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
