package models;

import annotations.Bind;

public abstract class Model {
    @Bind
    public int LL; // number of years for simulation
    protected String modelName;
    protected String description;// optional field for model identification

    // Constructor
    public Model(String modelName, String description) {
        this.modelName = modelName;
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    // Abstract method for running the model's calculation
    public abstract void run();

    // Getter for the model name
    public String getModelName() {
        return modelName;
    }

    // Setter for simulation length
    public void setSimulationLength(int LL) {
        this.LL = LL;
    }

    // Getter for simulation length
    public int getSimulationLength() {
        return LL;
    }

    // Optional method for printing basic information
    public void printModelInfo() {
        System.out.println("Model: " + modelName);
        System.out.println("Simulation Length: " + LL + " years");
    }
}
