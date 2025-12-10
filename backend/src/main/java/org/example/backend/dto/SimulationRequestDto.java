package org.example.backend.dto;

import java.util.Map;

public class SimulationRequestDto {
    private String modelName;
    private Map<String, double[]> inputData;
    private int ll;

    public SimulationRequestDto() {
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Map<String, double[]> getInputData() {
        return inputData;
    }

    public void setInputData(Map<String, double[]> inputData) {
        this.inputData = inputData;
    }

    public int getLl() {
        return ll;
    }

    public void setLl(int ll) {
        this.ll = ll;
    }
}
