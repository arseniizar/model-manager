package simulation.api.dto;

public class SimulationResultDto {
    private int timeStep;
    private String variableName;
    private double value;

    public SimulationResultDto(int timeStep, String variableName, double value) {
        this.timeStep = timeStep;
        this.variableName = variableName;
        this.value = value;
    }

    public SimulationResultDto() {
    }

    public int getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
