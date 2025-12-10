package models;

import annotations.Bind;

public abstract class Model {
    @Bind
    public int LL;
    protected String modelName;
    protected String description;


    public Model(String modelName, String description) {
        this.modelName = modelName;
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }


    public abstract void run();


    public String getModelName() {
        return modelName;
    }


    public void setSimulationLength(int LL) {
        this.LL = LL;
    }


    public int getSimulationLength() {
        return LL;
    }


    public void printModelInfo() {
        System.out.println("Model: " + modelName);
        System.out.println("Simulation Length: " + LL + " years");
    }
}
