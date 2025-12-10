package org.example.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponseDto {
    private Long simulationRunId;
    private String status;
    private String message;
}
