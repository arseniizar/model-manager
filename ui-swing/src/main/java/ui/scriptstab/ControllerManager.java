package ui.scriptstab;

import controller.Controller;

public class ControllerManager {

    private final Controller controller;

    public ControllerManager(String modelName) {
        try {
            this.controller = new Controller(modelName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Controller for model: " + modelName, e);
        }
    }

    public Controller getController() {
        return controller;
    }
}
