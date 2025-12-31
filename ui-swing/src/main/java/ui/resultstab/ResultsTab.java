package ui.resultstab;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import simulation.api.dto.SavedResultDto;
import ui.AppConfig;
import ui.ConfigLoader;
import ui.GroupedListCellRenderer;
import ui.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsTab {

    private final JList<Object> resultFilesList;
    private final JTextArea resultContentArea;
    private final Map<String, SavedResultDto> dbResultsMap = new HashMap<>();

    public ResultsTab() {
        this.resultFilesList = new JList<>();
        this.resultFilesList.setCellRenderer(new GroupedListCellRenderer());
        this.resultFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.resultContentArea = new JTextArea();
        this.resultContentArea.setEditable(false);
        this.resultContentArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    }

    public JPanel createPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());

        JLabel resultsHeader = new JLabel(" Results");
        resultsHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        resultsHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultsHeader.setIcon(new FlatSVGIcon(getClass().getResource("/svgs/dir/projectDirectory_dark.svg")));

        JScrollPane resultsScrollPane = new JScrollPane(resultFilesList);
        JScrollPane contentScrollPane = new JScrollPane(resultContentArea);

        setupResultSelectionListener();
        loadResults(); // Початкове завантаження

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsScrollPane, contentScrollPane);
        splitPane.setResizeWeight(0.3);

        resultsPanel.add(resultsHeader, BorderLayout.NORTH);
        resultsPanel.add(splitPane, BorderLayout.CENTER);

        return resultsPanel;
    }

    public void loadResults() {
        DefaultListModel<Object> listModel = new DefaultListModel<>();

        listModel.addElement("---HEADER_OS---");
        DefaultListModel<String> fileResults = Utilities.loadResultsList();
        for (int i = 0; i < fileResults.size(); i++) {
            listModel.addElement(fileResults.getElementAt(i));
        }

        listModel.addElement("---HEADER_DB---");
        new SwingWorker<List<SavedResultDto>, Void>() {
            @Override
            protected List<SavedResultDto> doInBackground() throws Exception {
                OkHttpClient client = new OkHttpClient();
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                String apiUrl = ConfigLoader.getInstance().getBackendApiUrl().replace("/simulations", "/storage/results");
                Request request = new Request.Builder().url(apiUrl).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Failed to load results from DB: " + response.message());
                    return objectMapper.readValue(response.body().string(), new TypeReference<List<SavedResultDto>>() {});
                }
            }

            @Override
            protected void done() {
                try {
                    List<SavedResultDto> dbResults = get();
                    dbResultsMap.clear();
                    if (dbResults.isEmpty()) {
                        listModel.addElement(" (No results in DB)");
                    } else {
                        for (SavedResultDto result : dbResults) {
                            String displayName = String.format("DB: %s (ID: %d)", result.getName(), result.getId());
                            dbResultsMap.put(displayName, result);
                            listModel.addElement(displayName);
                        }
                    }
                } catch (Exception e) {
                    listModel.addElement(" (Error loading from DB)");
                    e.printStackTrace();
                }
                resultFilesList.setModel(listModel);
            }
        }.execute();
    }

    private void setupResultSelectionListener() {
        resultFilesList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            Object selectedItem = resultFilesList.getSelectedValue();
            resultContentArea.setText("");

            if (!(selectedItem instanceof String selectedText)) return;

            if (dbResultsMap.containsKey(selectedText)) {
                loadResultContentFromDb(dbResultsMap.get(selectedText));
            } else if (!selectedText.startsWith("---") && !selectedText.contains("(")) {
                loadResultContentFromFile(selectedText);
            }
        });
    }

    private void loadResultContentFromDb(SavedResultDto resultInfo) {
        resultContentArea.setText("Loading result from database...");
        new SwingWorker<SavedResultDto, Void>() {
            @Override
            protected SavedResultDto doInBackground() throws Exception {
                OkHttpClient client = new OkHttpClient();
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                String apiUrl = ConfigLoader.getInstance().getBackendApiUrl().replace("/simulations", "/storage/results/") + resultInfo.getId();
                Request request = new Request.Builder().url(apiUrl).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Failed to load result content: " + response.message());
                    return objectMapper.readValue(response.body().string(), SavedResultDto.class);
                }
            }

            @Override
            protected void done() {
                try {
                    SavedResultDto fullResult = get();
                    resultContentArea.setText(fullResult.getContent());
                } catch (Exception ex) {
                    resultContentArea.setText("// Error loading result content from DB: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadResultContentFromFile(String fileName) {
        try {
            String path = AppConfig.getResultFilePath(fileName);
            String content = Files.readString(Paths.get(path));
            resultContentArea.setText(content);
        } catch (IOException ex) {
            resultContentArea.setText("// Could not load result file: " + ex.getMessage());
        }
    }

    public JList<Object> getResultFilesList() {
        return resultFilesList;
    }
}
