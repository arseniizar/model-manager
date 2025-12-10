package org.example.backend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SimulationRequestDto {
    private String modelName;
    private Map<String, double[]> inputData;

    private int ll;
}
