package org.example.backend.controller;

import org.example.backend.dto.SimulationResponseDto;
import org.example.backend.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.backend.dto.SimulationRequestDto;
import java.util.Map;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/run")
    public ResponseEntity<?> runSimulation(@RequestBody SimulationRequestDto request) {
        try {
            Long runId = simulationService.runSimulation(request);
            SimulationResponseDto responseDto = new SimulationResponseDto(
                    runId,
                    "SUBMITTED",
                    "Simulation run started successfully."
            );
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            SimulationResponseDto errorResponse = new SimulationResponseDto(
                    null,
                    "FAILED",
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
