package ui.scriptstab;

import annotations.Bind;
import controller.Controller;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class ConfigurationPanel {

    private final Controller controller;
    private final RSyntaxTextArea configArea;

    public ConfigurationPanel(Controller controller) {
        this.controller = controller;
        this.configArea = new RSyntaxTextArea();
        initializeConfigArea();
    }

    public JPanel createPanel() {
        Utilities.setupSyntaxHighlighting(configArea);

        configArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_INI);

        RTextScrollPane configScrollPane = new RTextScrollPane(configArea);
        configScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        configScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        Utilities.setupSyntaxHighlighting(configArea);

        updateConfiguration();

        var res = Utilities.createPanelWithHeader(
                "Configuration",
                "/svgs/config/config_dark.svg",
                null,
                null,
                configScrollPane
        );

        res.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        return res;
    }

    private void initializeConfigArea() {
        configArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_INI);
        configArea.setCodeFoldingEnabled(false);
        configArea.setEditable(false);
        configArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    }

    public void updateConfiguration() {
        try {
            StringBuilder configContent = new StringBuilder();


            String currentData = controller.getCurrentDataPath() != null ? controller.getCurrentDataPath() : "None";
            String currentModel = controller.getModel() != null
                    ? controller.getModel().getClass().getSimpleName()
                    : "None";

            configContent.append("| Hint: If you see an error about an absent field, make sure you ran to the model first |").append("\n\n");
            configContent.append("Current Data: ").append(currentData).append("\n");
            configContent.append("Current Model: ").append(currentModel).append("\n\n");


            configContent.append("Model Fields:\n");
            Class<?> modelClass = controller.getModel().getClass();
            while (modelClass != null) {
                for (Field field : modelClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Bind.class)) {
                        field.setAccessible(true);
                        configContent.append("- ").append(field.getName())
                                .append(" (").append(field.getType().getSimpleName()).append(")\n");
                    }
                }
                modelClass = modelClass.getSuperclass();
            }

            configArea.setText(configContent.toString());
        } catch (Exception ex) {
            configArea.setText("Error loading configuration: " + ex.getMessage());
        }
    }


}
