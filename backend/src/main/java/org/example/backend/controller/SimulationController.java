package org.example.backend.controller;

import lombok.Data;
import org.example.backend.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Data
    static class SimulationRequest {
        private String modelName;
        private Map<String, double[]> inputData;
    }

    @PostMapping("/run")
    public ResponseEntity<?> runSimulation(@RequestBody SimulationRequest request) {
        try {
            Long runId = simulationService.runSimulation(request.getModelName(), request.getInputData());
            return ResponseEntity.ok(Map.of("simulationRunId", runId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
