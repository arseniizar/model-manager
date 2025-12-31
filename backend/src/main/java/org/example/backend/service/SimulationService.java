package org.example.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Controller;
import org.example.backend.dto.SimulationRequestDto;
import org.example.backend.entity.SimulationResult;
import org.example.backend.entity.SimulationRun;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.kafka.SimulationProducer;
import org.example.backend.repository.SimulationResultRepository;
import org.example.backend.repository.SimulationRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simulation.api.dto.SimulationResultDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private final SimulationRunRepository runRepository;
    private final SimulationResultRepository resultRepository;
    private final SimulationProducer simulationProducer;

    public SimulationService(SimulationRunRepository runRepository,
                             SimulationResultRepository resultRepository,
                             SimulationProducer simulationProducer) {
        this.runRepository = runRepository;
        this.resultRepository = resultRepository;
        this.simulationProducer = simulationProducer;
    }

    @Transactional
    public Long queueSimulation(SimulationRequestDto simulationRequestDto) {
        SimulationRun simulationRun = new SimulationRun();

        simulationRun.setStartTime(LocalDateTime.now());
        simulationRun.setModelName(simulationRequestDto.getModelName());
        simulationRun.setStatus("QUEUED");
        simulationRun =  runRepository.save(simulationRun);

        simulationProducer.sendSimulationRequest(simulationRun.getId(),  simulationRequestDto);

        return simulationRun.getId();
    }

    @Transactional
    public void processSimulation(Long runId, SimulationRequestDto requestDto) {
        SimulationRun simulationRun = runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation run not found"));

        simulationRun.setStatus("RUNNING");
        runRepository.save(simulationRun);

        executeSimulationLogic(simulationRun, requestDto);
    }

    @Transactional
    public Long runSimulation(SimulationRequestDto requestDto) {
        SimulationRun run = new SimulationRun();
        run.setModelName(requestDto.getModelName());
        run.setStartTime(LocalDateTime.now());
        run.setStatus("RUNNING");
        run = runRepository.save(run);

        executeSimulationLogic(run, requestDto);

        return run.getId();
    }

    private void executeSimulationLogic(SimulationRun run, SimulationRequestDto requestDto) {
        try {
            Controller controller = new Controller(requestDto.getModelName());
            controller.setBindField("LL", requestDto.getLl());
            if (requestDto.getInputData() != null) {
                for (Map.Entry<String, double[]> entry : requestDto.getInputData().entrySet()) {
                    controller.setBindField(entry.getKey(), entry.getValue());
                }
            }
            controller.runModel();

            String resultsTsv = controller.getResultsAsTsv();
            List<SimulationResult> resultsToSave = parseAndPrepareResults(resultsTsv, run);
            resultRepository.saveAll(resultsToSave);

            run.setStatus("COMPLETED");
            runRepository.save(run);

        } catch (Exception e) {
            run.setStatus("FAILED");
            runRepository.save(run);
            System.err.println("Simulation failed for runId " + run.getId() + ". Reason: " + e.getMessage());
            throw new RuntimeException("Simulation failed for model '" + requestDto.getModelName() + "'.", e);
        }
    }

    private List<SimulationResult> parseAndPrepareResults(String tsvData, SimulationRun run) {
        List<SimulationResult> results = new ArrayList<>();
        String[] lines = tsvData.split("\n");

        if (lines.length == 0 || !lines[0].startsWith("LATA")) {
            throw new IllegalArgumentException("Invalid TSV format: 'LATA' header not found.");
        }
        int ll = Integer.parseInt(lines[0].split("\t")[1]);

        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split("\t");
            if (parts.length < 1) continue;

            String variableName = parts[0];

            for (int t = 0; t < ll; t++) {
                int dataIndex = t + 1;
                if (dataIndex < parts.length) {
                    try {
                        double value = Double.parseDouble(parts[dataIndex]);

                        SimulationResult res = new SimulationResult();
                        res.setSimulationRun(run);
                        res.setVariableName(variableName);
                        res.setTimeStep(t);
                        res.setValue(value);
                        results.add(res);

                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse value for " + variableName + " at time step " + t + ". Value: " + parts[dataIndex]);
                    }
                }
            }
        }
        return results;
    }

    public List<SimulationRun> findAllCompletedRuns() {
        return runRepository.findAll().stream()
                .filter(run -> "COMPLETED".equals(run.getStatus()))
                .sorted(Comparator.comparing(SimulationRun::getStartTime).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SimulationResultDto> findResultsByRunId(Long runId) {
        if (!runRepository.existsById(runId)) {
            throw new ResourceNotFoundException("SimulationRun not found with id: " + runId);
        }

        List<SimulationResult> results = resultRepository.findBySimulationRunId(runId);

        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private SimulationResultDto convertToDto(SimulationResult result) {
        SimulationResultDto dto = new SimulationResultDto();
        dto.setVariableName(result.getVariableName());
        dto.setTimeStep(result.getTimeStep());
        dto.setValue(result.getValue());
        return dto;
    }
}
