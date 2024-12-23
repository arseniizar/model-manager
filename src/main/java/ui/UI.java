package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import ui.resultstab.ResultsTab;
import ui.scriptstab.ConfigurationPanel;
import ui.scriptstab.ControllerManager;
import ui.scriptstab.ScriptsTab;

import javax.swing.*;
import java.awt.*;

public class UI extends JFrame {

    public UI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setTitle("Model and Script Manager");
        setSize(screenSize.width, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        ControllerManager controllerManager = new ControllerManager("ExampleModel");
        ConfigurationPanel configurationPanel = new ConfigurationPanel(controllerManager.getController());
        controllerManager.getController().setOnConfigurationChanged(configurationPanel::updateConfiguration);

        ModelsTab modelsTab = new ModelsTab();
        ResultsTab resultsTab = new ResultsTab();
        ScriptsTab scriptsTab = new ScriptsTab(controllerManager, resultsTab, configurationPanel);


        tabbedPane.addTab("Models",
                new FlatSVGIcon(getClass().getResource("/svgs/dir/projectDirectory_dark.svg")),
                modelsTab.createPanel(controllerManager));
        tabbedPane.addTab("Scripts",
                new FlatSVGIcon(getClass().getResource("/svgs/dir/projectDirectory_dark.svg")),
                scriptsTab.createPanel());
        tabbedPane.addTab("Results",
                new FlatSVGIcon(getClass().getResource("/svgs/dir/projectDirectory_dark.svg")),
                resultsTab.createPanel());

        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 16));

        add(tabbedPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> modelsTab.selectDefaultModel("ExampleModel"));
        SwingUtilities.invokeLater(() -> modelsTab.selectDefaultData("data1.txt"));
    }
}
