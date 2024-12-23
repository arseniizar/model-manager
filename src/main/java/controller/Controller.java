package controller;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;

import annotations.Bind;
import com.formdev.flatlaf.FlatDarkLaf;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import ui.UI;

public class Controller {
    private Object model;
    private String modelName;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, double[]> dynamicFields = new HashMap<>();
    private String currentDataPath;
    private Runnable onConfigurationChanged;// Field for storing the current data path

    public Controller(String modelName) throws Exception {
        initializeModel(modelName);
    }

    /**
     * Initializes the model dynamically using reflection.
     */
    private void initializeModel(String modelName) throws Exception {
        try {
            this.modelName = "models." + modelName; // Adjust for package
            this.model = Class.forName(this.modelName).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Model class '" + modelName + "' not found.");
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to load model class '" + modelName + "': " + ex.getMessage());
        }
    }

    public void setOnConfigurationChanged(Runnable listener) {
        this.onConfigurationChanged = listener;
    }

    /**
     * Sets the current data path.
     */
    public void setCurrentDataPath(String dataPath) {
        this.currentDataPath = dataPath;
    }

    /**
     * Checks if a field is annotated with @Bind and belongs to the model.
     */
    private boolean isModelField(String fieldName) {
        try {
            Field field = findFieldInHierarchy(fieldName);
            return field != null && field.isAnnotationPresent(Bind.class);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Gets the current data path.
     */
    public String getCurrentDataPath() {
        return this.currentDataPath;
    }

    /**
     * Reads data from the file set in the current data path.
     */
    public void readDataFromCurrentPath() throws Exception {
        if (currentDataPath == null || currentDataPath.isEmpty()) {
            throw new IllegalArgumentException("Data file path is not set.");
        }
        readDataFrom(currentDataPath);
    }

    /**
     * Reads data from a specified file.
     */
    public void readDataFrom(String fileName) throws Exception {
        this.currentDataPath = fileName; // Update the current data path when reading data
        File file = new File(fileName); // Support absolute or relative file paths
        if (!file.exists()) throw new FileNotFoundException("Data file not found: " + fileName);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts[0].equals("LATA")) {
                    int LL = parts.length - 1;
                    setBindField("LL", LL);
                } else {
                    double[] values = new double[getBindFieldValue("LL") != null ? (int) getBindFieldValue("LL") : 1];
                    for (int i = 1; i < parts.length; i++) {
                        values[i - 1] = Double.parseDouble(parts[i]);
                    }
                    setBindField(parts[0], values);

                    if (onConfigurationChanged != null) {
                        onConfigurationChanged.run();
                    }
                }
            }
        }
    }

    /**
     * Runs the model.
     */
    public Controller runModel() throws Exception {
        model.getClass().getMethod("run").invoke(model);
        if (onConfigurationChanged != null) {
            onConfigurationChanged.run();
        }
        return this;
    }

    /**
     * Gets the model's description.
     */
    public String getModelDescription() {
        try {
            Method getDescriptionMethod = model.getClass().getMethod("getDescription");
            return (String) getDescriptionMethod.invoke(model);
        } catch (Exception e) {
            e.printStackTrace();
            return "No description available for this model.";
        }
    }

    /**
     * Gets the current model instance.
     */
    public Object getModel() {
        return model;
    }

    /**
     * Sets and initializes a new model by name.
     */
    public Controller setModel(String modelName) throws Exception {
        initializeModel(modelName);
        return this;
    }

    /**
     * Runs a Groovy script from a file.
     */
    public Controller runScriptFromFile(String scriptFileName) throws Exception {
        File file = new File(scriptFileName); // Adjusted for direct file access
        if (!file.exists()) {
            throw new FileNotFoundException("Script file not found: " + scriptFileName);
        }

        StringBuilder script = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                script.append(line).append("\n");
            }
        }

        return runScript(script.toString());
    }

    private void validateScript(String script) throws IllegalArgumentException {
        try {
            // Create a new GroovyShell for validation
            GroovyShell validationShell = new GroovyShell();
            validationShell.parse(script); // Parse the script for syntax errors
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Groovy script: " + ex.getMessage(), ex);
        }
    }


    /**
     * Runs a Groovy script.
     */
    public Controller runScript(String script) throws Exception {
        validateScript(script);

        Binding binding = new Binding();

        // Use reflection to ensure all fields in the inheritance hierarchy are initialized
        Class<?> clazz = model.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Bind.class)) {
                    field.setAccessible(true);
                    Object value = field.get(model);

                    // Dynamically initialize arrays
                    if (value == null && field.getType().isArray() && field.getType().getComponentType() == double.class) {
                        int LL = getBindFieldValue("LL") != null ? (int) getBindFieldValue("LL") : 0;
                        if (LL > 0) {
                            field.set(model, new double[LL]); // Initialize array
                            value = field.get(model); // Update value
                        } else {
                            throw new IllegalStateException("Field '" + field.getName() + "' is an uninitialized array but LL is not set.");
                        }
                    }

                    binding.setVariable(field.getName(), value);
                }
            }
            clazz = clazz.getSuperclass(); // Traverse to the superclass
        }

        // Include dynamic fields in the binding
        for (Map.Entry<String, double[]> entry : dynamicFields.entrySet()) {
            binding.setVariable(entry.getKey(), entry.getValue());
        }

        binding.setVariable("LL", getBindFieldValue("LL")); // Ensure LL is available

        // Modify GroovyShell to handle divisions and operations gracefully
        GroovyShell shell = new GroovyShell(binding);
        shell.setProperty("safeDivide", (BiFunction<Double, Double, Double>) (a, b) -> b == 0 ? 0.0 : a / b); // Safe divide function

        // Add helper script to allow safe division
        String helperScript = "def safeDivide(a, b) { return b == 0 ? 0 : a / b }";

        // Run the script
        shell.evaluate(helperScript);
        shell.evaluate(script);

        // Reflectively update the model fields based on script execution
        clazz = model.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Bind.class)) {
                    String varName = field.getName();
                    field.setAccessible(true);
                    Object value = binding.getVariable(varName);
                    if (value instanceof double[]) {
                        double[] values = (double[]) value;

                        // Validate the values (ensure no NaN or Infinity)
                        validateValues(values, varName);

                        // Update model field
                        field.set(model, values);
                    }
                }
            }
            clazz = clazz.getSuperclass(); // Traverse to the superclass
        }

        // Update dynamic fields based on script execution
        for (Object key : binding.getVariables().keySet()) {
            String varName = (String) key;
            if (!varName.matches("[a-z]") && !isModelField(varName)) {
                Object value = binding.getVariable(varName);
                if (value instanceof double[]) {
                    double[] values = (double[]) value;

                    // Validate the values (ensure no NaN or Infinity)
                    validateValues(values, varName);

                    // Update dynamic fields or initialize new ones
                    dynamicFields.put(varName, values);
                }
            }
        }

        return this;
    }


    /**
     * Gets model results as TSV.
     */
    public String getResultsAsTsv() throws Exception {
        StringBuilder result = new StringBuilder("LATA\t" + getBindFieldValue("LL") + "\n");

        // Include @annotations.Bind fields
        for (Field field : model.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Bind.class) && field.getType().isArray() && field.getType().getComponentType() == double.class) {
                field.setAccessible(true);
                result.append(field.getName()).append("\t");
                double[] values = (double[]) field.get(model);
                for (double val : values) {
                    result.append(val).append("\t");
                }
                result.append("\n");
            }
        }

        for (Map.Entry<String, double[]> entry : dynamicFields.entrySet()) {
            result.append(entry.getKey()).append("\t");
            for (double val : entry.getValue()) {
                result.append(val).append("\t");
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * Validates values in a double array for NaN or infinity.
     */
    private void validateValues(double[] values, String fieldName) {
        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i]) || Double.isInfinite(values[i])) {
                System.err.println("Validation failed for field: " + fieldName);
                System.err.println("Index: " + i + ", Value: " + values[i]);
                throw new IllegalArgumentException("Invalid value in field '" + fieldName + "': NaN or Infinity detected at index " + i);
            }
        }
    }

    /**
     * Gets a field value from the class hierarchy.
     */
    private Object getBindFieldValue(String fieldName) throws Exception {
        Field field = findFieldInHierarchy(fieldName);
        if (field == null) throw new NoSuchFieldException("Field not found: " + fieldName);
        if (field.isAnnotationPresent(Bind.class)) {
            field.setAccessible(true);
            return field.get(model);
        }
        throw new IllegalArgumentException("Field " + fieldName + " is not annotated with @Bind");
    }

    /**
     * Sets a field value in the class hierarchy.
     */
    private void setBindField(Object fieldName, Object value) throws Exception {
        Field field = findFieldInHierarchy((String) fieldName);
        if (field == null) throw new NoSuchFieldException("Field not found: " + fieldName);
        if (field.isAnnotationPresent(Bind.class)) {
            field.setAccessible(true);
            field.set(model, value);
        }
    }

    /**
     * Finds a field in the class hierarchy.
     */
    private Field findFieldInHierarchy(String fieldName) {
        Class<?> clazz = model.getClass();
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }


    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            UI ui = new UI();
            ui.setVisible(true);

            JOptionPane.showMessageDialog(
                    ui,
                    """
                            Welcome!
                            
                            ExampleModel was selected as the default and has been executed.
                            data1.txt was selected as the default and loaded successfully.
                            """,
                    "Welcome",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
}
