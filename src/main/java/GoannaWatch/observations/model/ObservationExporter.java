package GoannaWatch.observations.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Exports observations to a CSV file.
 */
public class ObservationExporter {

    private static final String HEADER =
            "id,observer,location,latitude,longitude,animal,endangered,date,favourite";

    /**
     * Writes the observations to the given file.
     *
     * @param observations the observations to write
     * @param destination  the CSV file to create
     * @throws IOException if the file cannot be written
     */
    public void export(List<Observation> observations, Path destination) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Observation o : observations) {
                writer.write(String.join(",",
                        String.valueOf(o.getId()),
                        text(o.getObserver().getFullName()),
                        text(o.getLocation()),
                        o.getLatitude() == null ? "" : o.getLatitude().toString(),
                        o.getLongitude() == null ? "" : o.getLongitude().toString(),
                        text(o.getAnimalSeen()),
                        text(o.getIsEndangered()),
                        o.getObservedAt().toString(),
                        String.valueOf(o.getIsFavourite())));
                writer.newLine();
            }
        }
    }

    /**
     * Makes values safe to place in CSV cell.
     * Values containing commas, quotes or line breaks are quoted.
     * @param value
     * @return
     */
    private String text(String value) {
        if (value == null) {
            return "";
        }
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
            value = "'" + value;
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
