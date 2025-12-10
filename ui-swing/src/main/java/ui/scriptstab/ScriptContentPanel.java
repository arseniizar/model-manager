package ui.scriptstab;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import controller.Controller;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import ui.Utilities;

import static ui.AppConfig.RESULTS_PATH;

public class ScriptContentPanel {

    private final RSyntaxTextArea chosenScriptArea;
    private final RSyntaxTextArea writableScriptArea;
    private final JButton chosenRunButton;
    private final JButton writableRunButton;
    private final ControllerManager controllerManager;
    private final JButton saveButton;

    public ScriptContentPanel(ControllerManager controllerManager, JList<String> scriptsJList, JList<String> resultsJList) {
        this.controllerManager = controllerManager;
        chosenScriptArea = Utilities.createSyntaxTextArea();
        writableScriptArea = Utilities.createSyntaxTextArea();
        chosenRunButton = Utilities.createStyledRunButton();
        writableRunButton = Utilities.createStyledRunButton();
        saveButton = Utilities.createSaveButton("/svgs/shell/shell_dark.svg",
                Utilities.createSaveScriptListener(writableScriptArea,
                        () -> {
                            scriptsJList.setModel(Utilities.loadScriptList());
                        },
                        () -> {
                            resultsJList.setModel(Utilities.loadResultsList());
                        }
                ));

        Utilities.setupSyntaxHighlighting(chosenScriptArea);
        Utilities.setupSyntaxHighlighting(writableScriptArea);

        chosenScriptArea.setEditable(false);
    }

    public JPanel createPanel() {

        JPanel chosenScriptPanel = Utilities.createPanelWithHeader(
                "Chosen Script Content",
                "/svgs/groovy/groovy_dark.svg",
                chosenRunButton,
                new RTextScrollPane(chosenScriptArea)
        );


        JPanel writableScriptPanel = Utilities.createPanelWithHeader(
                "Write a Script",
                "/svgs/config/config_dark.svg",
                writableRunButton,
                saveButton,
                new RTextScrollPane(writableScriptArea)
        );


        JSplitPane scriptSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chosenScriptPanel, writableScriptPanel);
        scriptSplitPane.setResizeWeight(0.5);
        scriptSplitPane.setDividerSize(8);


        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainContentPanel.add(scriptSplitPane, BorderLayout.CENTER);

        return mainContentPanel;
    }

    public void setRunListener(Consumer<String> scriptOutputListener, JList<String> scriptsJList, JList<String> resultsJList) {
        chosenRunButton.addActionListener(e -> handleChosenScript(scriptOutputListener, scriptsJList, resultsJList));
        writableRunButton.addActionListener(e -> handleWritableScript(scriptOutputListener, resultsJList));
    }

    private void handleChosenScript(Consumer<String> scriptOutputListener, JList<String> scriptsJList, JList<String> resultsJList) {
        try {
            String chosenScriptContent = chosenScriptArea.getText();

            if (chosenScriptContent == null || chosenScriptContent.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "The chosen script is empty. Please select or write a valid script.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (!isValidGroovyScript(chosenScriptContent)) {
                JOptionPane.showMessageDialog(
                        null,
                        "The chosen script is invalid. Please fix syntax errors before running.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Controller controller = controllerManager.getController();
            controller.runScript(chosenScriptContent);

            String scriptName = scriptsJList.getSelectedValue();
            if (scriptName == null || scriptName.isEmpty()) {
                scriptName = "chosenScript";
            }
            String timestamp = String.valueOf(System.currentTimeMillis());
            String resultFileName = "res_" + scriptName.replace(".groovy", "") + "_" + timestamp + ".txt";
            String resultFilePath = RESULTS_PATH + resultFileName;
            Files.write(Paths.get(resultFilePath), controller.getResultsAsTsv().getBytes());

            scriptOutputListener.accept("Script executed successfully:\n" + controller.getResultsAsTsv());
            JOptionPane.showMessageDialog(
                    null,
                    "Script executed successfully. Result saved as " + resultFileName,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );


            Utilities.reloadResultsList(resultsJList);

        } catch (Exception ex) {
            scriptOutputListener.accept("Error running chosen script: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleWritableScript(Consumer<String> scriptOutputListener, JList<String> resultsJList) {
        try {
            String writableScriptContent = writableScriptArea.getText();

            if (writableScriptContent == null || writableScriptContent.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "The writable script is empty. Please write a valid script.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (!isValidGroovyScript(writableScriptContent)) {
                System.out.println("HELLOO???");
                JOptionPane.showMessageDialog(
                        null,
                        "The writable script is invalid. Please fix syntax errors before running.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Controller controller = controllerManager.getController();
            controller.runScript(writableScriptContent);

            String scriptName = JOptionPane.showInputDialog(
                    null,
                    "Enter a name for the result file:",
                    "Save Writable Script Result",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (scriptName == null || scriptName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Result file name cannot be empty.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String timestamp = String.valueOf(System.currentTimeMillis());
            if (!scriptName.endsWith(".txt")) {
                scriptName += ".txt";
            }
            String resultFileName = "res_" + scriptName.replace(".txt", "") + "_" + timestamp + ".txt";
            String resultFilePath = RESULTS_PATH + resultFileName;
            Files.write(Paths.get(resultFilePath), controller.getResultsAsTsv().getBytes());

            scriptOutputListener.accept("Script executed successfully:\n" + controller.getResultsAsTsv());
            JOptionPane.showMessageDialog(
                    null,
                    "Script executed successfully. Result saved as " + resultFileName,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );


            Utilities.reloadResultsList(resultsJList);

        } catch (Exception ex) {
            scriptOutputListener.accept("Error running writable script: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean isValidGroovyScript(String scriptContent) {
        try {
            new groovy.lang.GroovyShell().parse(scriptContent);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void updateSelectedScript(String scriptContent) {
        chosenScriptArea.setText(scriptContent);
    }

    public RSyntaxTextArea getChosenScriptArea() {
        return chosenScriptArea;
    }
}
