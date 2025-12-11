package ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simulation.api.dto.SimulationRunDto;
import ui.scriptstab.ControllerManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static ui.AppConfig.DATA_PATH;

public class ModelsTab {
    private ControllerManager controllerManager;
    private JList<Object> modelsJList;
    private JList<Object> dataJList;
    private Map<String, SimulationRunDto> dbRunsMap = new HashMap<>();

    public JPanel createPanel(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;

        modelsJList = new JList<>();
        modelsJList.setCellRenderer(new CustomCellRenderers.ModelCellRenderer());
        modelsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadModelsIntoList();

        this.dataJList = new JList<>();
        dataJList.setCellRenderer(new GroupedListCellRenderer());
        dataJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadDataIntoList();

        JPanel rightPanel = createRightPanel(modelsJList, dataJList);

        JSplitPane dataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(modelsJList), new JScrollPane(dataJList));
        dataSplitPane.setResizeWeight(0.5);

        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel dataHeader = new JLabel(" Data and Models");
        dataHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        dataHeader.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/classgrp/groupByClass_dark.svg")));
        dataHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.add(dataHeader, BorderLayout.NORTH);
        leftPanel.add(dataSplitPane, BorderLayout.CENTER);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplitPane.setResizeWeight(0.15);

        JPanel modelsPanel = new JPanel(new BorderLayout());
        modelsPanel.add(mainSplitPane, BorderLayout.CENTER);

        ActionPanel actionPanel = new ActionPanel(controllerManager.getController());
        modelsPanel.add(actionPanel.createPanel(), BorderLayout.SOUTH);

        return modelsPanel;
    }

    private void loadModelsIntoList() {
        DefaultListModel<Object> model = new DefaultListModel<>();
        DefaultListModel<String> loadedModels = Utilities.loadModelList();
        for (int i = 0; i < loadedModels.size(); i++) {
            model.addElement(loadedModels.getElementAt(i));
        }
        modelsJList.setModel(model);
    }

    private void loadDataIntoList() {
        DefaultListModel<Object> listModel = new DefaultListModel<>();

        listModel.addElement("---HEADER_OS---");
        File dataFolder = new File(AppConfig.DATA_PATH);
        if (dataFolder.exists() && dataFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(dataFolder.listFiles((dir, name) -> name.endsWith(".txt")))) {
                listModel.addElement(file.getName());
            }
        }

        listModel.addElement("---HEADER_DB---");

        new SwingWorker<List<SimulationRunDto>, Void>() {
            @Override
            protected List<SimulationRunDto> doInBackground() throws Exception {
                OkHttpClient client = new OkHttpClient();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                String apiUrl = ConfigLoader.getInstance().getBackendApiUrl() + "/runs";
                Request request = new Request.Builder().url(apiUrl).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to load runs from server: " + response);
                    }
                    SimulationRunDto[] runs = objectMapper.readValue(response.body().string(), SimulationRunDto[].class);
                    return Arrays.asList(runs);
                }
            }

            @Override
            protected void done() {
                try {
                    List<SimulationRunDto> dbRuns = get();
                    if (dbRuns.isEmpty()) {
                        listModel.addElement(" (No runs found in DB)");
                    } else {
                        dbRunsMap.clear();
                        for (SimulationRunDto run : dbRuns) {
                            String displayName = String.format("Run #%d: %s (%s)", run.getId(), run.getModelName(),
                                    run.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                            dbRunsMap.put(displayName, run);
                            listModel.addElement(displayName);
                        }
                    }
                } catch (Exception e) {
                    listModel.addElement(" (Error loading from DB)");
                    e.printStackTrace();
                }
            }
        }.execute();

        dataJList.setModel(listModel);
    }

    private JPanel createRightPanel(JList<Object> modelsJList, JList<Object> dataJList) {
        JTextArea descriptionArea = new JTextArea();
        JPanel descriptionPanel = Utilities.createDescriptionPanel(descriptionArea);

        JPanel codePanel = createCodePanel();
        JPanel dataContentPanel = Utilities.createDataContentPanel();

        JSplitPane descriptionSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, descriptionPanel, dataContentPanel);
        descriptionSplitPane.setResizeWeight(0.5);

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, descriptionSplitPane, codePanel);
        rightSplitPane.setResizeWeight(0.25);

        Utilities.setupModelSelectionListener(modelsJList, descriptionPanel,
                (RSyntaxTextArea) ((RTextScrollPane) codePanel.getComponent(1)).getViewport().getView(),
                controllerManager.getController(), descriptionArea);

        Utilities.setupDataSelectionListener(dataJList, dataContentPanel, controllerManager.getController(), dbRunsMap);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(rightSplitPane, BorderLayout.CENTER);

        return rightPanel;
    }

    private JPanel createCodePanel() {
        JPanel codePanel = new JPanel(new BorderLayout());
        JLabel codeHeader = new JLabel(" Java Code");
        codeHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        codeHeader.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/jav/java_dark.svg")));
        codeHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        codePanel.add(codeHeader, BorderLayout.NORTH);

        RSyntaxTextArea modelCode = new RSyntaxTextArea();
        modelCode.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        modelCode.setCodeFoldingEnabled(true);
        modelCode.setEditable(false);

        try (InputStream themeStream = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml")) {
            Theme theme = Theme.load(themeStream);
            theme.apply(modelCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RTextScrollPane codeScrollPane = new RTextScrollPane(modelCode);
        codePanel.add(codeScrollPane, BorderLayout.CENTER);
        return codePanel;
    }

    public void selectDefaultModel(String defaultModelName) {
        SwingUtilities.invokeLater(() -> {
            ListModel<Object> listModel = modelsJList.getModel();
            int targetIndex = -1;


            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.getElementAt(i).equals(defaultModelName)) {
                    targetIndex = i;
                    break;
                }
            }

            if (targetIndex != -1) {
                modelsJList.setSelectedIndex(targetIndex);
                String selectedModel = modelsJList.getSelectedValue().toString();
                if (selectedModel != null) {
                    try {
                        controllerManager.getController().setModel(selectedModel);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Error running default model: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Default model '" + defaultModelName + "' not found in the list.",
                        "Model Not Found",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
    }

    public void selectDefaultData(String defaultDataFileName) {
        SwingUtilities.invokeLater(() -> {
            ListModel<Object> listModel = dataJList.getModel();
            int targetIndex = -1;


            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.getElementAt(i).equals(defaultDataFileName)) {
                    targetIndex = i;
                    break;
                }
            }

            if (targetIndex != -1) {
                dataJList.setSelectedIndex(targetIndex);
                String selectedDataFile = dataJList.getSelectedValue().toString();
                if (selectedDataFile != null) {
                    try {
                        controllerManager.getController().readDataFrom(DATA_PATH + selectedDataFile);


                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Error loading default data: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Default data file '" + defaultDataFileName + "' not found in the list.",
                        "Data File Not Found",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
    }
}
