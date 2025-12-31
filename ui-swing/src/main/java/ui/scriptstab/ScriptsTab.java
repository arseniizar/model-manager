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
    private final ConfigurationPanel configurationPanel;

    public ScriptsTab(ControllerManager controllerManager, ResultsTab resultsTab, ConfigurationPanel configurationPanel) {
        this.controllerManager = controllerManager;
        this.resultsTab = resultsTab;
        this.scriptListPanel = new ScriptListPanel(controllerManager);

        this.scriptContentPanel = new ScriptContentPanel(
                controllerManager,
                scriptListPanel.getScriptsList(),
                scriptListPanel.getDbScriptsMap(),
                scriptListPanel::loadScripts,
                resultsTab::loadResults
        );
        this.scriptOutputPanel = new ScriptOutputPanel();
        this.configurationPanel = configurationPanel;
    }

    public JPanel createPanel() {
        JPanel scriptsPanel = new JPanel(new BorderLayout());


        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scriptContentPanel.createPanel(), null);
        contentSplitPane.setResizeWeight(0.7);


        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                contentSplitPane, configurationPanel.createPanel());
        leftSplitPane.setResizeWeight(0.8);
        leftSplitPane.setDividerSize(5);


        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                leftSplitPane, scriptOutputPanel.createPanel());
        mainSplitPane.setResizeWeight(0.5);


        scriptsPanel.add(scriptListPanel.createPanel(scriptOutputPanel::updateOutput), BorderLayout.WEST);
        scriptsPanel.add(mainSplitPane, BorderLayout.CENTER);

        scriptContentPanel.setRunListener(scriptOutputPanel::updateOutput);

        return scriptsPanel;
    }


}
