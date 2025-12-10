package ui;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final ConfigLoader INSTANCE = new ConfigLoader();
    private final Properties properties;

    private ConfigLoader() {
        properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getBackendApiUrl() {
        return properties.getProperty("backend.api.url", "http://localhost:8080/api/simulations");
    }
}
