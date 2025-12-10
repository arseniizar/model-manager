package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import ui.scriptstab.ControllerManager;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

import static ui.AppConfig.DATA_PATH;

public class ModelsTab {
    private ControllerManager controllerManager;
    private JList<String> modelsJList;
    private JList<String> dataJList;

    public JPanel createPanel(ControllerManager controllerManager) {
        JPanel modelsPanel = new JPanel(new BorderLayout());

        modelsJList = new JList<>(Utilities.loadModelList());
        modelsJList.setCellRenderer(new CustomCellRenderers.ModelCellRenderer());
        modelsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.controllerManager = controllerManager;

        this.dataJList = new JList<>(Utilities.loadDataList());
        dataJList.setCellRenderer(new CustomCellRenderers.DataCellRenderer());
        dataJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        modelsPanel.add(mainSplitPane, BorderLayout.CENTER);

        ActionPanel actionPanel = new ActionPanel(controllerManager.getController());
        modelsPanel.add(actionPanel.createPanel(), BorderLayout.SOUTH);

        return modelsPanel;
    }

    private JPanel createRightPanel(JList<String> modelsJList, JList<String> dataJList) {
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

        Utilities.setupDataSelectionListener(dataJList, dataContentPanel, controllerManager.getController());

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
            ListModel<String> listModel = modelsJList.getModel();
            int targetIndex = -1;


            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.getElementAt(i).equals(defaultModelName)) {
                    targetIndex = i;
                    break;
                }
            }

            if (targetIndex != -1) {
                modelsJList.setSelectedIndex(targetIndex);
                String selectedModel = modelsJList.getSelectedValue();
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
            ListModel<String> listModel = dataJList.getModel();
            int targetIndex = -1;


            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.getElementAt(i).equals(defaultDataFileName)) {
                    targetIndex = i;
                    break;
                }
            }

            if (targetIndex != -1) {
                dataJList.setSelectedIndex(targetIndex);
                String selectedDataFile = dataJList.getSelectedValue();
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
