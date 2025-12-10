package org.example.backend.service;

import controller.Controller;
import org.example.backend.entity.SimulationResult;
import org.example.backend.entity.SimulationRun;
import org.example.backend.repository.SimulationResultRepository;
import org.example.backend.repository.SimulationRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SimulationService {

    private final SimulationRunRepository runRepository;

    private final SimulationResultRepository resultRepository;

    public SimulationService(SimulationResultRepository simulationResultRepository, SimulationRunRepository runRepository) {
        this.resultRepository = simulationResultRepository;
        this.runRepository = runRepository;
    }

    @Transactional
    public Long runSimulation(String modelName, Map<String, double[]> inputData) {
        SimulationRun run = new SimulationRun();
        run.setModelName(modelName);
        run.setStartTime(LocalDateTime.now());
        run.setStatus("RUNNING");
        run = runRepository.save(run);

        try {
            Controller controller = new Controller(modelName);

            Integer ll = null;
            for (Map.Entry<String, double[]> entry : inputData.entrySet()) {
                controller.setBindField(entry.getKey(), entry.getValue());
                if ("LL".equals(entry.getKey())) {
                    ll = (int) entry.getValue()[0];
                } else {
                    controller.setBindField(entry.getKey(), entry.getValue());
                }
            }
            if (ll != null) {
                controller.setBindField("LL", ll);
            } else {
                throw new IllegalArgumentException("LL (simulation length) must be provided in input data.");
            }


            controller.runModel();

            String resultsTsv = controller.getResultsAsTsv();
            List<SimulationResult> resultsToSave = parseAndPrepareResults(resultsTsv, run);
            resultRepository.saveAll(resultsToSave);

            run.setStatus("COMPLETED");
            runRepository.save(run);

            return run.getId();

        } catch (Exception e) {
            run.setStatus("FAILED");
            runRepository.save(run);
            throw new RuntimeException("Simulation failed: " + e.getMessage(), e);
        }
    }

    private List<SimulationResult> parseAndPrepareResults(String tsvData, SimulationRun run) {
        List<SimulationResult> results = new ArrayList<>();
        String[] lines = tsvData.split("\n");
        int ll = Integer.parseInt(lines[0].split("\t")[1]);

        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split("\t");
            String variableName = parts[0];
            for (int t = 0; t < ll; t++) {
                if (t + 1 < parts.length) {
                    double value = Double.parseDouble(parts[t + 1]);
                    SimulationResult res = new SimulationResult();
                    res.setSimulationRun(run);
                    res.setVariableName(variableName);
                    res.setTimeStep(t);
                    res.setValue(value);
                    results.add(res);
                }
            }
        }
        return results;
    }
}
