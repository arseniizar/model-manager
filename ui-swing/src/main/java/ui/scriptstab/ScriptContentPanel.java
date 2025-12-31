package ui.scriptstab;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.Controller;
import okhttp3.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import simulation.api.dto.SavedResultDto;
import simulation.api.dto.ScriptDto;
import ui.AppConfig;
import ui.ConfigLoader;
import ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;

public class ScriptContentPanel {

    private final RSyntaxTextArea chosenScriptArea;
    private final RSyntaxTextArea writableScriptArea;
    private final JButton chosenRunButton;
    private final JButton writableRunButton;
    private final ControllerManager controllerManager;
    private final JButton saveButton;

    private final JList<Object> scriptsJList;
    private final Map<String, ScriptDto> dbScriptsMap;
    private final Runnable onSaveSuccess;
    private final Runnable onResultSaveSuccess;

    public ScriptContentPanel(ControllerManager controllerManager,
                              JList<Object> scriptsJList,
                              Map<String, ScriptDto> dbScriptsMap,
                              Runnable onSaveSuccess,
                              Runnable onResultSaveSuccess) {
        this.controllerManager = controllerManager;
        this.scriptsJList = scriptsJList;
        this.dbScriptsMap = dbScriptsMap;
        this.onSaveSuccess = onSaveSuccess;
        this.onResultSaveSuccess = onResultSaveSuccess;

        this.chosenScriptArea = Utilities.createSyntaxTextArea();
        this.writableScriptArea = Utilities.createSyntaxTextArea();
        this.chosenRunButton = Utilities.createStyledRunButton();
        this.writableRunButton = Utilities.createStyledRunButton();
        this.saveButton = Utilities.createSaveButton("/svgs/shell/shell_dark.svg", createSaveScriptListener());

        Utilities.setupSyntaxHighlighting(chosenScriptArea);
        Utilities.setupSyntaxHighlighting(writableScriptArea);
        chosenScriptArea.setEditable(false);

        setupScriptSelectionListener();
    }

    public JPanel createPanel() {
        JPanel chosenScriptPanel = Utilities.createPanelWithHeader(
                "Chosen Script Content", "/svgs/groovy/groovy_dark.svg",
                chosenRunButton, new RTextScrollPane(chosenScriptArea));

        JPanel writableScriptPanel = Utilities.createPanelWithHeader(
                "Write a Script", "/svgs/config/config_dark.svg",
                writableRunButton, saveButton, new RTextScrollPane(writableScriptArea));

        JSplitPane scriptSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chosenScriptPanel, writableScriptPanel);
        scriptSplitPane.setResizeWeight(0.5);
        scriptSplitPane.setDividerSize(8);

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainContentPanel.add(scriptSplitPane, BorderLayout.CENTER);

        return mainContentPanel;
    }

    public void setRunListener(Consumer<String> scriptOutputListener) {
        chosenRunButton.addActionListener(e -> handleRunScript(chosenScriptArea.getText(), scriptOutputListener));
        writableRunButton.addActionListener(e -> handleRunScript(writableScriptArea.getText(), scriptOutputListener));
    }

    private void setupScriptSelectionListener() {
        scriptsJList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            Object selectedItem = scriptsJList.getSelectedValue();
            chosenScriptArea.setText(""); // Очищуємо поле перед завантаженням

            if (!(selectedItem instanceof String selectedText)) return;

            if (dbScriptsMap.containsKey(selectedText)) {
                loadScriptFromDb(dbScriptsMap.get(selectedText));
            } else if (!selectedText.startsWith("---") && !selectedText.contains("(")) {
                loadScriptFromFile(selectedText);
            }
        });
    }

    private void loadScriptFromDb(ScriptDto scriptInfo) {
        chosenScriptArea.setText("// Loading script from database...");
        new SwingWorker<ScriptDto, Void>() {
            @Override
            protected ScriptDto doInBackground() throws Exception {
                OkHttpClient client = new OkHttpClient();
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                String apiUrl = ConfigLoader.getInstance().getBackendApiUrl().replace("/simulations", "/storage/scripts/") + scriptInfo.getId();
                Request request = new Request.Builder().url(apiUrl).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Failed to load script content: " + response.message());
                    return objectMapper.readValue(response.body().string(), ScriptDto.class);
                }
            }

            @Override
            protected void done() {
                try {
                    ScriptDto fullScript = get();
                    chosenScriptArea.setText(fullScript.getContent());
                } catch (Exception ex) {
                    chosenScriptArea.setText("// Error loading script content from DB: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadScriptFromFile(String fileName) {
        try {
            String path = AppConfig.getScriptFilePath(fileName);
            String content = Files.readString(Paths.get(path));
            chosenScriptArea.setText(content);
        } catch (IOException ex) {
            chosenScriptArea.setText("// Could not load script file: " + ex.getMessage());
        }
    }

    private ActionListener createSaveScriptListener() {
        return e -> {
            String scriptContent = writableScriptArea.getText();
            if (scriptContent.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Script content cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String scriptName = JOptionPane.showInputDialog(null, "Enter the name for the script:", "Save Script", JOptionPane.PLAIN_MESSAGE);
            if (scriptName == null || scriptName.trim().isEmpty()) {
                return;
            }

            ScriptDto dto = new ScriptDto();
            dto.setName(scriptName);
            dto.setContent(scriptContent);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    OkHttpClient client = new OkHttpClient();
                    ObjectMapper objectMapper = new ObjectMapper();
                    String apiUrl = ConfigLoader.getInstance().getBackendApiUrl().replace("/simulations", "/storage/scripts");
                    RequestBody body = RequestBody.create(
                            objectMapper.writeValueAsString(dto),
                            MediaType.get("application/json; charset=utf-8")
                    );
                    Request request = new Request.Builder().url(apiUrl).post(body).build();
                    try (Response response = client.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new IOException("Failed to save script: " + response.body().string());
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(null, "Script saved to database successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        onSaveSuccess.run();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error saving script: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }.execute();
        };
    }

    private void handleRunScript(String scriptContent, Consumer<String> scriptOutputListener) {
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Script is empty and cannot be run.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Controller controller = controllerManager.getController();
            controller.runScript(scriptContent);
            String tsvResult = controller.getResultsAsTsv();

            String resultName = JOptionPane.showInputDialog(null, "Enter a name for the result file:", "Save Result", JOptionPane.PLAIN_MESSAGE);
            if (resultName != null && !resultName.trim().isEmpty()) {
                saveResultToDb(resultName, tsvResult);
            }

            scriptOutputListener.accept("Script executed successfully:\n" + tsvResult);

        } catch (Exception ex) {
            scriptOutputListener.accept("Error running script: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void saveResultToDb(String name, String content) {
        SavedResultDto dto = new SavedResultDto();
        dto.setName(name);
        dto.setContent(content);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                OkHttpClient client = new OkHttpClient();
                ObjectMapper objectMapper = new ObjectMapper();
                String apiUrl = ConfigLoader.getInstance().getBackendApiUrl().replace("/simulations", "/storage/results");
                RequestBody body = RequestBody.create(objectMapper.writeValueAsString(dto), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder().url(apiUrl).post(body).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to save result: " + response.body().string());
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(null, "Result '" + name + "' saved to database.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onResultSaveSuccess.run(); // Оновлюємо список результатів
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error saving result: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    public RSyntaxTextArea getChosenScriptArea() {
        return chosenScriptArea;
    }
}
