package org.example.backend.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.dto.SimulationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimulationProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${simulation.kafka.topic}")
    private String topicName;

    public SimulationProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendSimulationRequest(Long runId, SimulationRequestDto simulationRequestDto) {
        try {
            SimulationTaskMessage message = new SimulationTaskMessage(runId, simulationRequestDto);
            String json = objectMapper.writeValueAsString(message);
            System.out.println("simulation task json: " + json + "; topic name: " + topicName);
            kafkaTemplate.send(topicName, json);
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
    }

    public static class SimulationTaskMessage {
        public Long runId;
        public SimulationRequestDto simulationRequestDto;
        public SimulationTaskMessage(Long runId, SimulationRequestDto simulationRequestDto) {
            this.runId = runId;
            this.simulationRequestDto = simulationRequestDto;
        }
    }
}
