package ui.resultstab;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import ui.CustomCellRenderers;
import ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ui.AppConfig.RESULTS_PATH;

public class ResultsTab {

    private final JList<String> resultFilesList;

    public ResultsTab() {
        this.resultFilesList = new JList<>(Utilities.loadResultsList());
    }

    public JPanel createPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());

        JLabel resultsHeader = new JLabel(" Results");
        resultsHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        resultsHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultsHeader.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/dir/projectDirectory_dark.svg")));

        resultFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultFilesList.setFont(new Font("SansSerif", Font.PLAIN, 14));

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


        JScrollPane resultsScrollPane = new JScrollPane(resultFilesList);
        resultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        JTextArea resultContentArea = new JTextArea();
        resultContentArea.setEditable(false);
        resultContentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane contentScrollPane = new JScrollPane(resultContentArea);


        resultFilesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedResult = resultFilesList.getSelectedValue();
                if (selectedResult != null) {
                    try {
                        String path = RESULTS_PATH + selectedResult;
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


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsScrollPane, contentScrollPane);
        splitPane.setResizeWeight(0.3);

        resultsPanel.add(resultsHeader, BorderLayout.NORTH);
        resultsPanel.add(splitPane, BorderLayout.CENTER);

        return resultsPanel;
    }


    public JList<String> getResultFilesList() {
        return resultFilesList;
    }
}
