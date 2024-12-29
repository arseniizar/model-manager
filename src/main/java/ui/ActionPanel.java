package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import controller.Controller;

import javax.swing.*;
import java.awt.*;

public class ActionPanel {
    private final Controller controller;

    public ActionPanel(Controller controller) {
        this.controller = controller;
    }

    public JPanel createPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));


        JButton runModelButton = Utilities.createStyledRunButton();
        runModelButton.setText("Run Model");
        runModelButton.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/run/run_dark.svg")));
        runModelButton.addActionListener(e -> handleRunModel());


        JButton readDataButton = Utilities.createStyledRunButton();
        readDataButton.setText("Read from Data");
        readDataButton.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/run/run_dark.svg")));
        readDataButton.addActionListener(e -> handleReadData());


        actionPanel.add(readDataButton);
        actionPanel.add(runModelButton);

        return actionPanel;
    }

    private void handleRunModel() {
        try {
            controller.runModel();
            showMessage("Model has been successfully run!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showMessage("Error running model: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
