package ui;

import java.io.File;

public final class AppConfig {

    private AppConfig() {}

    public static final String MODELS_PATH = "simulation-core/src/main/java/models/";
    public static final String DATA_PATH = "ui-swing/src/main/resources/data/";
    public static final String SCRIPTS_PATH = "ui-swing/src/main/resources/scripts/";
    public static final String RESULTS_PATH = "ui-swing/src/main/resources/results/";

    public static String getDataFilePath(String fileName) {
        return DATA_PATH + fileName;
    }

    public static String getScriptFilePath(String fileName) {
        return SCRIPTS_PATH + fileName;
    }

    public static String getResultFilePath(String fileName) {
        return RESULTS_PATH + fileName;
    }
}
