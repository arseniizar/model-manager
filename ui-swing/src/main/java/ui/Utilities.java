package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import controller.Controller;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import simulation.api.dto.SimulationRunDto;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.List;

import static ui.AppConfig.*;


public class Utilities {
    public static DefaultListModel<String> loadModelList() {
        DefaultListModel<String> modelList = new DefaultListModel<>();
        File modelsFolder = new File(MODELS_PATH);
        if (modelsFolder.exists() && modelsFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(modelsFolder.listFiles((dir, name) -> name.endsWith(".java")))) {
                String modelName = file.getName().replace(".java", "");
                if (!modelName.equals("Model")) {
                    modelList.addElement(modelName);
                }
            }
        }
        return modelList;
    }

    public static DefaultListModel<String> loadDataList() {
        DefaultListModel<String> dataList = new DefaultListModel<>();
        File dataFolder = new File(DATA_PATH);
        if (dataFolder.exists() && dataFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(dataFolder.listFiles((dir, name) -> name.endsWith(".txt")))) {
                dataList.addElement(file.getName());
            }
        }
        return dataList;
    }

    public static DefaultListModel<String> loadResultsList() {
        DefaultListModel<String> resultsList = new DefaultListModel<>();
        File resultsFolder = new File(RESULTS_PATH);
        if (resultsFolder.exists() && resultsFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(resultsFolder.listFiles((dir, name) -> name.endsWith(".txt")))) {
                resultsList.addElement(file.getName());
            }
        }
        return resultsList;
    }


    public static DefaultListModel<String> loadScriptList() {
        DefaultListModel<String> scriptList = new DefaultListModel<>();
        File scriptsFolder = new File(SCRIPTS_PATH);
        if (scriptsFolder.exists() && scriptsFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(scriptsFolder.listFiles((dir, name) -> name.endsWith(".groovy")))) {
                scriptList.addElement(file.getName());
            }
        }
        return scriptList;
    }

    public static JPanel createDescriptionPanel(JTextArea descriptionArea) {
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        JLabel descriptionHeader = new JLabel(" Description");
        descriptionHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        descriptionHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionHeader.setIcon(new FlatSVGIcon(UI.class.getResource("/svgs/text/text_dark.svg")));
        descriptionPanel.add(descriptionHeader, BorderLayout.NORTH);

        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        descriptionPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        return descriptionPanel;
    }


    public static JPanel createDataContentPanel() {
        JPanel dataContentPanel = new JPanel(new BorderLayout());


        JLabel dataHeader = new JLabel(" Data Content");
        dataHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dataHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        dataHeader.setIcon(new FlatSVGIcon(UI.class.getResource("/svgs/text/text_dark.svg")));
        dataContentPanel.add(dataHeader, BorderLayout.NORTH);


        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


        JTable dataTable = new JTable(tableModel);
        dataTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dataTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        dataTable.setFillsViewportHeight(true);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        dataTable.setDefaultRenderer(Object.class, centerRenderer);


        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        dataTable.setPreferredScrollableViewportSize(new Dimension(dataTable.getPreferredSize().width, 120));

        dataContentPanel.add(scrollPane, BorderLayout.CENTER);


        dataContentPanel.putClientProperty("dataTable", dataTable);

        return dataContentPanel;
    }


    public static void updateDataContentPanel(JPanel dataContentPanel, String[][] data, String[] columnNames) {
        JTable dataTable = (JTable) dataContentPanel.getClientProperty("dataTable");
        if (dataTable != null) {

            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            dataTable.setModel(tableModel);


            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < dataTable.getColumnCount(); i++) {
                dataTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    public static void setupModelSelectionListener(
            JList<Object> modelsJList,
            JPanel descriptionPanel,
            RSyntaxTextArea modelCodeArea,
            Controller controller,
            JTextArea descriptionArea) {

        modelsJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Object selectedItem = modelsJList.getSelectedValue();

                if (selectedItem instanceof String selectedModel) {
                    try {
                        controller.setModel(selectedModel);
                        descriptionArea.setText(controller.getModelDescription());

                        String codePath = AppConfig.MODELS_PATH + selectedModel + ".java";
                        try {
                            String code = Files.readString(Paths.get(codePath));
                            modelCodeArea.setText(code);
                        } catch (IOException ex) {
                            modelCodeArea.setText("// Could not load source file from: " + codePath + "\n" +
                                    "// Source code is located in the 'simulation-core' module.");
                        }
                    } catch (Exception ex) {
                        descriptionArea.setText("Could not load model description for " + selectedModel);
                        modelCodeArea.setText("Could not load model code.");
                        ex.printStackTrace();
                    }
                } else {
                    descriptionArea.setText("");
                    modelCodeArea.setText("");
                }
            }
        });
    }

    public static void setupDataSelectionListener(
            JList<Object> dataJList,
            JPanel dataContentPanel,
            Controller controller,
            Map<String, SimulationRunDto> dbRunsMap) {

        dataJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Object selectedItem = dataJList.getSelectedValue();

                if (!(selectedItem instanceof String)) {
                    updateDataContentPanel(dataContentPanel, new String[0][0], new String[0]);
                    return;
                }

                String selectedText = (String) selectedItem;

                if (selectedText.startsWith("Run #")) {
                    SimulationRunDto selectedRun = dbRunsMap.get(selectedText);
                    if (selectedRun != null) {

                        String[][] data = {
                                {"ID:", String.valueOf(selectedRun.getId())},
                                {"Model:", selectedRun.getModelName()},
                                {"Started:", selectedRun.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))},
                                {"Status:", selectedRun.getStatus()}
                        };
                        String[] columnNames = {"Property", "Value"};

                        updateDataContentPanel(dataContentPanel, data, columnNames);
                        JOptionPane.showMessageDialog(null,
                                "Loading full results for past runs is not yet implemented.\n" +
                                        "This would require a new backend endpoint: GET /api/simulations/" + selectedRun.getId() + "/results",
                                "Feature not implemented",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                }
                else if (!selectedText.startsWith("---") && !selectedText.contains(" (")) {
                    try {
                        String path = AppConfig.getDataFilePath(selectedText);
                        controller.setCurrentDataPath(path);

                        List<String> lines = Files.readAllLines(Paths.get(path));
                        if (!lines.isEmpty()) {
                            String[] columnNames = lines.get(0).split("\\s+");
                            List<String[]> rows = new ArrayList<>();
                            for (int i = 1; i < lines.size(); i++) {
                                rows.add(lines.get(i).split("\\s+"));
                            }
                            String[][] data = rows.toArray(new String[0][]);
                            updateDataContentPanel(dataContentPanel, data, columnNames);
                        } else {
                            updateDataContentPanel(dataContentPanel, new String[0][0], new String[0]);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Could not load data file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    updateDataContentPanel(dataContentPanel, new String[0][0], new String[0]);
                }
            }
        });
    }


    public static void setupScriptSelectionListener(JList<String> scriptsJList, RSyntaxTextArea scriptCodeArea) {
        scriptsJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedScript = scriptsJList.getSelectedValue();
                if (selectedScript != null) {
                    try {
                        String path = SCRIPTS_PATH + selectedScript;
                        List<String> lines = Files.readAllLines(Paths.get(path));

                        // Find the first meaningful line (excluding package and empty lines)
                        int firstMeaningfulLineIndex = 0;
                        while (firstMeaningfulLineIndex < lines.size() &&
                                (lines.get(firstMeaningfulLineIndex).trim().isEmpty() ||
                                        lines.get(firstMeaningfulLineIndex).startsWith("package"))) {
                            firstMeaningfulLineIndex++;
                        }

                        // Collect lines, preserving original formatting after the first meaningful line
                        String content = lines.stream()
                                .skip(firstMeaningfulLineIndex) // Skip the unnecessary lines
                                .collect(Collectors.joining("\n"));

                        scriptCodeArea.setText(content);
                    } catch (IOException ex) {
                        scriptCodeArea.setText("Could not load script content.");
                    }
                } else {
                    scriptCodeArea.setText("");
                }
            }
        });
    }


    public static RSyntaxTextArea createSyntaxTextArea() {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        textArea.setCodeFoldingEnabled(true);
        return textArea;
    }

    public static JButton createStyledRunButton() {
        JButton runButton = new JButton("Run");
        runButton.setIcon(new FlatSVGIcon(Utilities.class.getResource("/svgs/run/run_dark.svg")));
        runButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        runButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return runButton;
    }

    public static void reloadScriptList(JList<String> scriptsJList) {
        DefaultListModel<String> listModel = new DefaultListModel<>();

        try {
            // Load all scripts in the directory
            Path scriptsDir = Paths.get(SCRIPTS_PATH);
            Files.list(scriptsDir)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .forEach(listModel::addElement);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error loading scripts: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }

        // Set the updated list model
        scriptsJList.setModel(listModel);
    }


    public static ActionListener createSaveScriptListener(
            RSyntaxTextArea scriptCodeArea,
            Runnable reloadScriptList,
            Runnable reloadResultsList
    ) {
        return e -> {
            String scriptContent = scriptCodeArea.getText();

            // Check if the script is empty
            if (scriptContent.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Script content cannot be empty.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Prompt for the script name
            String scriptName = JOptionPane.showInputDialog(
                    null,
                    "Enter the name of the script to save:",
                    "Save Script",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (scriptName != null && !scriptName.trim().isEmpty()) {
                // Ensure script ends with .groovy
                if (!scriptName.endsWith(".groovy")) {
                    scriptName += ".groovy";
                }

                try {
                    String fullScript = "package scripts\n\n" + scriptContent;

                    String outputPath = SCRIPTS_PATH + scriptName;
                    Files.write(Paths.get(outputPath), fullScript.getBytes());

                    JOptionPane.showMessageDialog(
                            null,
                            "Script saved successfully as " + scriptName,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Reload the script list
                    reloadScriptList.run();

                    // Reload the results list
                    reloadResultsList.run();

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Error saving script: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Script name cannot be empty.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        };
    }


    public static JButton createSaveButton(String iconPath, ActionListener saveListener) {
        JButton saveButton = new JButton("Save");
        saveButton.setIcon(new FlatSVGIcon(Utilities.class.getResource(iconPath)));
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveButton.setFocusable(false);
        saveButton.addActionListener(saveListener);
        return saveButton;
    }


    // Method for two buttons
    public static JPanel createPanelWithHeader(String title, String iconPath, JButton button1, JButton button2, JComponent component) {
        JLabel header = new JLabel(" " + title);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setIcon(new FlatSVGIcon(Utilities.class.getResource(iconPath)));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(header, BorderLayout.WEST);

        // Panel for buttons (aligned to the right)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        if (button1 != null) buttonPanel.add(button1);
        if (button2 != null) buttonPanel.add(button2);

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    // Method for one button
    public static JPanel createPanelWithHeader(String title, String iconPath, JButton button, JComponent component) {
        return createPanelWithHeader(title, iconPath, button, null, component); // Delegate to the two-button method
    }

    public static JPanel createPanelWithHeader(String title, String iconPath, JComponent component) {
        return createPanelWithHeader(title, iconPath, null, null, component); // Delegate to the two-button method
    }


    public static void setupSyntaxHighlighting(RSyntaxTextArea textArea) {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        textArea.setCodeFoldingEnabled(true);
        try (InputStream themeStream = Utilities.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml")) {
            Theme theme = Theme.load(themeStream);
            theme.apply(textArea);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isValidGroovyScript(String script) {
        try {
            // Create a GroovyShell instance with a blank binding
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);

            // Parse the script to check for syntax errors
            shell.parse(script);
            return true; // No syntax errors found
        } catch (Exception ex) {
            // Syntax error detected
            ex.printStackTrace();
            return false;
        }
    }

    public static void reloadResultsList(JList<String> resultsJList) {
        DefaultListModel<String> resultsModel = Utilities.loadResultsList();
        resultsJList.setModel(resultsModel);
        resultsJList.revalidate();
        resultsJList.repaint(); // Ensure the JList is repainted
    }

}
