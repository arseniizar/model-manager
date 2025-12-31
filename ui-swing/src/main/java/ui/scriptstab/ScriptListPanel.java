package ui.scriptstab;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import controller.Controller;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import simulation.api.dto.ScriptDto;
import ui.ConfigLoader;
import ui.CustomCellRenderers;
import ui.GroupedListCellRenderer;
import ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static ui.AppConfig.RESULTS_PATH;

public class ScriptListPanel {

    private final JList<Object> scriptsJList;
    private final ControllerManager controllerManager;
    private final Map<String, ScriptDto> dbScriptsMap = new HashMap<>();

    public ScriptListPanel(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;

        this.scriptsJList = new JList<>();
        this.scriptsJList.setCellRenderer(new GroupedListCellRenderer());
        this.scriptsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public JPanel createPanel(Consumer<String> scriptOutputListener) {
        JLabel scriptsHeader = new JLabel(" Scripts");
        scriptsHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        scriptsHeader.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/config/config_dark.svg")));
        scriptsHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton runButton = Utilities.createStyledRunButton();
        runButton.setText("Run From File");
        runButton.addActionListener(e -> openFileChooserAndRunScript(scriptOutputListener));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(scriptsHeader, BorderLayout.CENTER);
        headerPanel.add(runButton, BorderLayout.EAST);

        JPanel scriptsListPanel = new JPanel(new BorderLayout());
        scriptsListPanel.add(headerPanel, BorderLayout.NORTH);
        scriptsListPanel.add(new JScrollPane(scriptsJList), BorderLayout.CENTER);

        loadScripts();

        return scriptsListPanel;
    }

    public void loadScripts() {
        DefaultListModel<Object> listModel = new DefaultListModel<>();

        listModel.addElement("---HEADER_OS---");
        DefaultListModel<String> fileScripts = Utilities.loadScriptList();
        for (int i = 0; i < fileScripts.size(); i++) {
            listModel.addElement(fileScripts.getElementAt(i));
        }

        listModel.addElement("---HEADER_DB---");

        new SwingWorker<List<ScriptDto>, Void>() {
            @Override
            protected List<ScriptDto> doInBackground() throws Exception {
                OkHttpClient client = new OkHttpClient();
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                String apiUrl = ConfigLoader.getInstance().getBackendApiUrl()
                        .replace("/simulations", "/storage/scripts");
                Request request = new Request.Builder().url(apiUrl).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to load scripts from DB: " + response.message());
                    }
                    assert response.body() != null;
                    return objectMapper.readValue(response.body().string(), new TypeReference<List<ScriptDto>>() {
                    });
                }
            }

            @Override
            protected void done() {
                try {
                    List<ScriptDto> dbScripts = get();
                    dbScriptsMap.clear();
                    if (dbScripts.isEmpty()) {
                        listModel.addElement(" (No scripts in DB)");
                    } else {
                        for (ScriptDto script : dbScripts) {
                            String displayName = String.format("DB: %s (ID: %d)", script.getName(), script.getId());
                            dbScriptsMap.put(displayName, script);
                            listModel.addElement(displayName);
                        }
                    }
                } catch (Exception e) {
                    listModel.addElement(" (Error loading from DB)");
                    e.printStackTrace();
                }
                scriptsJList.setModel(listModel);
            }
        }.execute();
    }

    private void openFileChooserAndRunScript(Consumer<String> scriptOutputListener) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Script File to Run");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);


        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Groovy Scripts", "groovy"));

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                Path selectedFile = fileChooser.getSelectedFile().toPath();
                String scriptContent = Files.readString(selectedFile);


                if (!Utilities.isValidGroovyScript(scriptContent)) {
                    JOptionPane.showMessageDialog(
                            null,
                            "The script has syntax errors. Please fix them and try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }


                Controller controller = controllerManager.getController();
                controller.runScript(scriptContent);


                String scriptName = selectedFile.getFileName().toString().replace(".groovy", "");
                String timestamp = String.valueOf(System.currentTimeMillis());
                String resultFileName = "res_" + scriptName + "_" + timestamp + ".txt";
                String resultFilePath = RESULTS_PATH + resultFileName;
                Files.write(Path.of(resultFilePath), controller.getResultsAsTsv().getBytes());


                scriptOutputListener.accept("Script executed successfully:\n" + controller.getResultsAsTsv());

                JOptionPane.showMessageDialog(
                        null,
                        "Script executed successfully. Result saved as " + resultFileName,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error running script from file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        }
    }

    public JList<Object> getScriptsList() {
        return scriptsJList;
    }

    public Map<String, ScriptDto> getDbScriptsMap() {
        return dbScriptsMap;
    }
}
