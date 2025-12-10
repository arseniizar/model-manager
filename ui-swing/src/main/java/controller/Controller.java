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
    private final Map<String, double[]> dynamicFields = new HashMap<>();
    private String currentDataPath;
    private Runnable onConfigurationChanged;

    public Controller(String modelName) throws Exception {
        initializeModel(modelName);
    }

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

    public void setCurrentDataPath(String dataPath) {
        this.currentDataPath = dataPath;
    }

    private boolean isModelField(String fieldName) {
        try {
            Field field = findFieldInHierarchy(fieldName);
            return field != null && field.isAnnotationPresent(Bind.class);
        } catch (Exception e) {
            return false;
        }
    }


    public String getCurrentDataPath() {
        return this.currentDataPath;
    }

    public void readDataFromCurrentPath() throws Exception {
        if (currentDataPath == null || currentDataPath.isEmpty()) {
            throw new IllegalArgumentException("Data file path is not set.");
        }
        readDataFrom(currentDataPath);
    }

    public void readDataFrom(String fileName) throws Exception {
        this.currentDataPath = fileName;
        File file = new File(fileName);
        if (!file.exists()) throw new FileNotFoundException("Data file not found: " + fileName);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) break;
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


    public Controller runModel() throws Exception {
        model.getClass().getMethod("run").invoke(model);
        if (onConfigurationChanged != null) {
            onConfigurationChanged.run();
        }
        return this;
    }

    public String getModelDescription() {
        try {
            Method getDescriptionMethod = model.getClass().getMethod("getDescription");
            return (String) getDescriptionMethod.invoke(model);
        } catch (Exception e) {
            e.printStackTrace();
            return "No description available for this model.";
        }
    }

    public Object getModel() {
        return model;
    }

    public Controller setModel(String modelName) throws Exception {
        initializeModel(modelName);
        return this;
    }

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
            GroovyShell validationShell = new GroovyShell();
            validationShell.parse(script);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Groovy script: " + ex.getMessage(), ex);
        }
    }


    public Controller runScript(String script) throws Exception {
        validateScript(script);

        Binding binding = new Binding();

        Class<?> clazz = model.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Bind.class)) {
                    field.setAccessible(true);
                    Object value = field.get(model);

                    if (value == null && field.getType().isArray() && field.getType().getComponentType() == double.class) {
                        int LL = getBindFieldValue("LL") != null ? (int) getBindFieldValue("LL") : 0;
                        if (LL > 0) {
                            field.set(model, new double[LL]);
                            value = field.get(model);
                        } else {
                            throw new IllegalStateException("Field '" + field.getName() + "' is an uninitialized array but LL is not set.");
                        }
                    }

                    binding.setVariable(field.getName(), value);
                }
            }
            clazz = clazz.getSuperclass();
        }

        for (Map.Entry<String, double[]> entry : dynamicFields.entrySet()) {
            binding.setVariable(entry.getKey(), entry.getValue());
        }

        binding.setVariable("LL", getBindFieldValue("LL"));

        GroovyShell shell = new GroovyShell(binding);
        shell.setProperty("safeDivide", (BiFunction<Double, Double, Double>) (a, b) -> b == 0 ? 0.0 : a / b);

        String helperScript = "def safeDivide(a, b) { return b == 0 ? 0 : a / b }";

        shell.evaluate(helperScript);

        try {
            shell.evaluate(script);
        } catch (groovy.lang.MissingPropertyException e) {
            throw new IllegalArgumentException("Undefined variable in script: " + e.getProperty());
        }

        clazz = model.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Bind.class)) {
                    String varName = field.getName();
                    field.setAccessible(true);
                    Object value = binding.getVariable(varName);
                    if (value instanceof double[]) {
                        double[] values = (double[]) value;

                        validateValues(values, varName);

                        field.set(model, values);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        for (Object key : binding.getVariables().keySet()) {
            String varName = (String) key;
            if (!varName.matches("[a-z]") && !isModelField(varName)) {
                Object value = binding.getVariable(varName);
                if (value instanceof double[]) {
                    double[] values = (double[]) value;

                    validateValues(values, varName);

                    dynamicFields.put(varName, values);
                }
            }
        }

        return this;
    }


    public String getResultsAsTsv() throws Exception {
        StringBuilder result = new StringBuilder("LATA\t" + getBindFieldValue("LL") + "\n");

        for (Field field : model.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Bind.class) && field.getType().isArray() && field.getType().getComponentType() == double.class) {
                field.setAccessible(true);
                result.append(field.getName()).append("\t");
                double[] values = (double[]) field.get(model);
                if (values != null) {
                    for (double val : values) {
                        result.append(val).append("\t");
                    }
                } else {
                    result.append("null");
                }
                result.append("\n");
            }
        }

        for (Map.Entry<String, double[]> entry : dynamicFields.entrySet()) {
            result.append(entry.getKey()).append("\t");
            if (entry.getValue() != null) {
                for (double val : entry.getValue()) {
                    result.append(val).append("\t");
                }
            } else {
                result.append("null");
            }
            result.append("\n");
        }

        return result.toString();
    }


    private void validateValues(double[] values, String fieldName) {
        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i]) || Double.isInfinite(values[i])) {
                System.err.println("Validation failed for field: " + fieldName);
                System.err.println("Index: " + i + ", Value: " + values[i]);
                throw new IllegalArgumentException("Invalid value in field '" + fieldName + "': NaN or Infinity detected at index " + i);
            }
        }
    }

    public Object getBindFieldValue(String fieldName) throws Exception {
        Field field = findFieldInHierarchy(fieldName);
        if (field == null) throw new NoSuchFieldException("Field not found: " + fieldName);
        if (field.isAnnotationPresent(Bind.class)) {
            field.setAccessible(true);
            return field.get(model);
        }
        throw new IllegalArgumentException("Field " + fieldName + " is not annotated with @Bind");
    }

    public void setBindField(Object fieldName, Object value) throws Exception {
        Field field = findFieldInHierarchy((String) fieldName);
        if (field == null) throw new NoSuchFieldException("Field not found: " + fieldName);
        if (field.isAnnotationPresent(Bind.class)) {
            field.setAccessible(true);
            if ("LL".equals(fieldName)) {
                int LLValue = (int) value;
                if (LLValue <= 0) {
                    throw new IllegalArgumentException("LL must be greater than zero.");
                }
            }
            field.set(model, value);
        }
    }

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
