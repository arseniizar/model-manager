package org.example.backend.service;

import controller.Controller;
import org.example.backend.dto.SimulationRequestDto;
import org.example.backend.entity.SimulationRun;
import org.example.backend.repository.SimulationResultRepository;
import org.example.backend.repository.SimulationRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private SimulationRunRepository runRepository;

    @Mock
    private SimulationResultRepository resultRepository;

    @InjectMocks
    private SimulationService simulationService;

    @Test
    void testRunSimulation_Success() throws Exception {
        int ll = 5;

        SimulationRequestDto requestDto = new SimulationRequestDto();
        requestDto.setModelName("ExampleModel");
        requestDto.setLl(ll);

        Map<String, double[]> inputData = new HashMap<>();
        inputData.put("KI", new double[ll]);
        inputData.put("KS", new double[ll]);
        inputData.put("INW", new double[ll]);
        inputData.put("EKS", new double[ll]);
        inputData.put("IMP", new double[ll]);
        inputData.put("twKI", new double[ll]);
        inputData.put("twKS", new double[ll]);
        inputData.put("twINW", new double[ll]);
        inputData.put("twEKS", new double[ll]);
        inputData.put("twIMP", new double[ll]);

        inputData.get("KI")[0] = 100.0;
        inputData.get("KS")[0] = 200.0;

        requestDto.setInputData(inputData);

        when(runRepository.save(any(SimulationRun.class))).thenAnswer(invocation -> {
            SimulationRun originalRun = invocation.getArgument(0);

            SimulationRun savedRun = new SimulationRun();
            savedRun.setId(originalRun.getId());
            savedRun.setModelName(originalRun.getModelName());
            savedRun.setStatus(originalRun.getStatus());
            savedRun.setStartTime(originalRun.getStartTime());

            if (savedRun.getId() == null) {
                savedRun.setId(1L);
            }

            return savedRun;
        });

        Long simulationId = simulationService.runSimulation(requestDto);

        assertNotNull(simulationId);
        assertEquals(1L, simulationId);

        ArgumentCaptor<SimulationRun> runCaptor = ArgumentCaptor.forClass(SimulationRun.class);

        verify(runRepository, times(2)).save(runCaptor.capture());

        SimulationRun firstSave = runCaptor.getAllValues().get(0);
        assertEquals("RUNNING", firstSave.getStatus());

        SimulationRun secondSave = runCaptor.getAllValues().get(1);
        assertEquals("COMPLETED", secondSave.getStatus());

        verify(resultRepository, times(1)).saveAll(any());
    }

    @Test
    void testRunSimulation_Failure() {
        SimulationRequestDto requestDto = new SimulationRequestDto();
        requestDto.setModelName("NonExistentModel");
        requestDto.setLl(5);
        requestDto.setInputData(new HashMap<>());

        when(runRepository.save(any(SimulationRun.class))).thenAnswer(invocation -> {
            SimulationRun run = invocation.getArgument(0);
            run.setId(1L);
            return run;
        });

        assertThrows(RuntimeException.class, () -> {
            simulationService.runSimulation(requestDto);
        });

        ArgumentCaptor<SimulationRun> runCaptor = ArgumentCaptor.forClass(SimulationRun.class);
        verify(runRepository, times(2)).save(runCaptor.capture());

        SimulationRun finalSave = runCaptor.getAllValues().get(1);
        assertEquals("FAILED", finalSave.getStatus());

        verify(resultRepository, never()).saveAll(any());
    }
}
