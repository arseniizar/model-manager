package ui.scriptstab;

import ui.Utilities;
import ui.resultstab.ResultsTab;

import javax.swing.*;
import java.awt.*;

public class ScriptsTab {

    private final ControllerManager controllerManager;
    private final ScriptListPanel scriptListPanel;
    private final ScriptContentPanel scriptContentPanel;
    private final ScriptOutputPanel scriptOutputPanel;
    private final ResultsTab resultsTab;
    private final ConfigurationPanel configurationPanel; // Add ConfigurationPanel

    public ScriptsTab(ControllerManager controllerManager, ResultsTab resultsTab, ConfigurationPanel configurationPanel) {
        this.controllerManager = controllerManager;
        this.resultsTab = resultsTab;
        this.scriptListPanel = new ScriptListPanel(controllerManager, resultsTab.getResultFilesList());
        this.scriptContentPanel = new ScriptContentPanel(controllerManager, scriptListPanel.getScriptsList(), resultsTab.getResultFilesList());
        this.scriptOutputPanel = new ScriptOutputPanel();
        this.configurationPanel = configurationPanel; // Initialize ConfigurationPanel
    }

    public JPanel createPanel() {
        JPanel scriptsPanel = new JPanel(new BorderLayout());

        // Vertical split pane for "Chosen Script Content" and "Write a Script"
        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scriptContentPanel.createPanel(), null);
        contentSplitPane.setResizeWeight(0.7); // Allocate more space to script content

        // Horizontal split pane for "Content Split" on the left and "Configuration" on the right
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                contentSplitPane, configurationPanel.createPanel());
        leftSplitPane.setResizeWeight(0.8); // Allocate 80% to content and 20% to configuration
        leftSplitPane.setDividerSize(5);

        // Create a vertical split pane for "Left Split (Content and Configuration)" and "Script Output"
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                leftSplitPane, scriptOutputPanel.createPanel());
        mainSplitPane.setResizeWeight(0.5); // Split evenly between top (Content + Configuration) and bottom (Script Output)

        // Add left panel for script list and right panel for main content
        scriptsPanel.add(scriptListPanel.createPanel(scriptOutputPanel::updateOutput), BorderLayout.WEST);
        scriptsPanel.add(mainSplitPane, BorderLayout.CENTER);

        // Setup interaction
        Utilities.setupScriptSelectionListener(scriptListPanel.getScriptsList(), scriptContentPanel.getChosenScriptArea());

        scriptContentPanel.setRunListener(scriptOutputPanel::updateOutput,
                scriptListPanel.getScriptsList(),
                resultsTab.getResultFilesList()
        );


        return scriptsPanel;
    }


}
