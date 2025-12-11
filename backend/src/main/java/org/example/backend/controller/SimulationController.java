package org.example.backend.controller;

import org.example.backend.dto.SimulationResponseDto;
import org.example.backend.entity.SimulationRun;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.backend.dto.SimulationRequestDto;
import simulation.api.dto.SimulationResultDto;

import java.util.List;
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

    @GetMapping("/runs")
    public ResponseEntity<List<SimulationRun>> getAllRuns() {
        List<SimulationRun> runs = simulationService.findAllCompletedRuns();
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/{runId}/results")
    public ResponseEntity<List<SimulationResultDto>> getResultsForRun(@PathVariable("runId") Long runId) {
        try {
            List<SimulationResultDto> results = simulationService.findResultsByRunId(runId);
            return ResponseEntity.ok(results);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
