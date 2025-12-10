package ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import controller.Controller;
import okhttp3.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ActionPanel {
    private final Controller controller;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public ActionPanel(Controller controller) {
        this.controller = controller;
    }

    public JPanel createPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton readDataButton = Utilities.createStyledRunButton();
        readDataButton.setText("Read from Data");
        readDataButton.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/run/run_dark.svg")));
        readDataButton.addActionListener(e -> handleReadData());

        JButton runModelButton = Utilities.createStyledRunButton();
        runModelButton.setText("Run Model");
        runModelButton.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/run/run_dark.svg")));
        runModelButton.addActionListener(e -> handleRunModel());

        actionPanel.add(readDataButton);
        actionPanel.add(runModelButton);

        return actionPanel;
    }

    private void handleRunModel() {
        try {
            int ll = (int) controller.getBindFieldValue("LL");
            if (ll <= 0) {
                showMessage("Please load data first by clicking 'Read from Data' before running the model.", "Data Not Loaded", JOptionPane.WARNING_MESSAGE);
                return;
            }

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    String modelName = controller.getModel().getClass().getSimpleName();
                    int ll = (int) controller.getBindFieldValue("LL");

                    Map<String, double[]> inputData = collectInputArrays();

                    Map<String, Object> requestDto = new HashMap<>();
                    requestDto.put("modelName", modelName);
                    requestDto.put("ll", ll);
                    requestDto.put("inputData", inputData);

                    String jsonBody = objectMapper.writeValueAsString(requestDto);

                    RequestBody body = RequestBody.create(jsonBody, JSON);

                    String apiUrl = ConfigLoader.getInstance().getBackendApiUrl();
                    if (apiUrl == null || apiUrl.isEmpty()) {
                        throw new IOException("Backend API URL is not configured in config.properties");
                    }

                    Request request = new Request.Builder()
                            .url(apiUrl + "/run")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        String responseBodyString = response.body().string();
                        if (!response.isSuccessful()) {
                            throw new IOException("Server returned error: " + response.code() + " - " + responseBodyString);
                        }
                        return responseBodyString;
                    }
                }

                @Override
                protected void done() {
                    try {
                        String responseBody = get();
                        showMessage("Simulation started successfully!\nResponse: " + responseBody, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        showMessage("Error running model: \n" + ex.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }.execute();

        } catch (Exception ex) {
            showMessage("An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private Map<String, double[]> collectInputArrays() throws Exception {
        Map<String, double[]> arrays = new HashMap<>();
        Object model = controller.getModel();
        for (Field field : model.getClass().getDeclaredFields()) {
            if (field.getType().isArray() && field.getType().getComponentType() == double.class) {
                field.setAccessible(true);
                Object value = field.get(model);
                if (value != null) {
                    arrays.put(field.getName(), (double[]) value);
                }
            }
        }
        return arrays;
    }

    private void handleReadData() {
        try {
            controller.readDataFromCurrentPath();
            showMessage("Data has been successfully read from: " + controller.getCurrentDataPath(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showMessage("Error reading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(null, message, title, messageType);
    }
}
