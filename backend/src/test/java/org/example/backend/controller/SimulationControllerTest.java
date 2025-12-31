package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.dto.SimulationRequestDto;
import org.example.backend.service.SimulationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SimulationController.class)
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SimulationService simulationService;

    @Test
    void testQueueSimulation_Success() throws Exception {
        SimulationRequestDto requestDto = new SimulationRequestDto();
        requestDto.setModelName("ExampleModel");
        requestDto.setLl(5);
        requestDto.setInputData(new HashMap<>());

        when(simulationService.queueSimulation(any(SimulationRequestDto.class))).thenReturn(1L);

        mockMvc.perform(post("/api/simulations/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.simulationRunId").value(1L))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testQueueSimulation_QueueingFailure() throws Exception {
        SimulationRequestDto requestDto = new SimulationRequestDto();
        requestDto.setModelName("BadModel");
        requestDto.setLl(5);

        String errorMessage = "Failed to send message to Kafka";
        when(simulationService.queueSimulation(any(SimulationRequestDto.class)))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/api/simulations/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED_TO_QUEUE"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}
