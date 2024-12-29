package ui.scriptstab;

import javax.swing.*;
import java.awt.*;

public class ScriptOutputPanel {

    private final JTextArea outputArea;

    public ScriptOutputPanel() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
    }

    public JPanel createPanel() {

        JLabel outputHeader = new JLabel(" Script Output");
        outputHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        outputHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        outputArea.setEditable(false);
        outputArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);


        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        scrollPane.setPreferredSize(new Dimension(800, 200));


        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(outputHeader, BorderLayout.NORTH);
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        return outputPanel;
    }

    public void updateOutput(String output) {
        outputArea.setText(output);
    }

}
