package org.example.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.service.SimulationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SimulationConsumer {
    private final SimulationService simulationService;
    public final ObjectMapper objectMapper;

    public SimulationConsumer(SimulationService simulationService, ObjectMapper objectMapper) {
        this.simulationService = simulationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${simulation.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSimulationTask(String json) {
        try {
            System.out.println("Received simulation task: " + json);
            SimulationProducer.SimulationTaskMessage message =
                    objectMapper.readValue(json, SimulationProducer.SimulationTaskMessage.class);
//            simulationService.
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
