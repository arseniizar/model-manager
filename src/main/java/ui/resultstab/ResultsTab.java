package ui.resultstab;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import ui.CustomCellRenderers;
import ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResultsTab {

    private final JList<String> resultFilesList;

    public ResultsTab() {
        this.resultFilesList = new JList<>(Utilities.loadResultsList());
    }

    public JPanel createPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());

        // Add an icon to the header
        JLabel resultsHeader = new JLabel(" Results");
        resultsHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        resultsHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultsHeader.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/dir/projectDirectory_dark.svg"))); // Header icon

        // Create the JList for displaying results
        resultFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultFilesList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Set a custom renderer to add icons to the list items
        resultFilesList.setCellRenderer(new DefaultListCellRenderer() {
            private final Icon resultIcon = new FlatSVGIcon(getClass().getResource("/svgs/text/text_dark.svg"));

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setIcon(resultIcon);
                label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
                return label;
            }
        });

        // Wrap the JList in a JScrollPane for scrolling
        JScrollPane resultsScrollPane = new JScrollPane(resultFilesList);
        resultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Display the content of the selected result on the right
        JTextArea resultContentArea = new JTextArea();
        resultContentArea.setEditable(false);
        resultContentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane contentScrollPane = new JScrollPane(resultContentArea);

        // Add selection listener to the results list
        resultFilesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedResult = resultFilesList.getSelectedValue();
                if (selectedResult != null) {
                    try {
                        String path = "src/main/resources/results/" + selectedResult;
                        String content = Files.readString(Paths.get(path));
                        resultContentArea.setText(content);
                    } catch (IOException ex) {
                        resultContentArea.setText("Could not load result file.");
                    }
                } else {
                    resultContentArea.setText("");
                }
            }
        });

        // Create a split pane to divide results list and result content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsScrollPane, contentScrollPane);
        splitPane.setResizeWeight(0.3); // Allocate 30% to the results list and 70% to the content area

        resultsPanel.add(resultsHeader, BorderLayout.NORTH);
        resultsPanel.add(splitPane, BorderLayout.CENTER);

        return resultsPanel;
    }


    public JList<String> getResultFilesList() {
        return resultFilesList;
    }
}
