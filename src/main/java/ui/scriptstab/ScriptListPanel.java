package ui.scriptstab;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import controller.Controller;
import ui.CustomCellRenderers;
import ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ScriptListPanel {

    private final JList<String> scriptsJList;
    private final ControllerManager controllerManager;
    private final JList<String> resultsJList;

    public ScriptListPanel(ControllerManager controllerManager, JList<String> resultsJList) {
        this.controllerManager = controllerManager;
        scriptsJList = new JList<>(Utilities.loadScriptList());
        scriptsJList.setCellRenderer(new CustomCellRenderers.ScriptCellRenderer());
        scriptsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.resultsJList = resultsJList;
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

        return scriptsListPanel;
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
                String resultFilePath = "src/main/resources/results/" + resultFileName;
                Files.write(Path.of(resultFilePath), controller.getResultsAsTsv().getBytes());


                scriptOutputListener.accept("Script executed successfully:\n" + controller.getResultsAsTsv());

                JOptionPane.showMessageDialog(
                        null,
                        "Script executed successfully. Result saved as " + resultFileName,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );


               Utilities.reloadResultsList(resultsJList);
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

    public JList<String> getScriptsList() {
        return scriptsJList;
    }
}
