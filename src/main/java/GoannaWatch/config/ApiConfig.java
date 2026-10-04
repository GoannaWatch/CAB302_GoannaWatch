package GoannaWatch.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Reads API keys from config.properties in the project root (which is git-ignored).
 */
public final class ApiConfig {

    private ApiConfig() {}

    /** @return the Google Maps API key, or null if it can't be read */
    public static String getGoogleMapsKey() {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Path.of("config.properties"))) {
            props.load(in);
            return props.getProperty("google.maps.api.key");
        } catch (IOException e) {
            return null;
        }
    }
}