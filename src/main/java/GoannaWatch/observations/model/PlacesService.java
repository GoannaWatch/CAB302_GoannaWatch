package GoannaWatch.observations.model;

import GoannaWatch.config.ApiConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Looks up places using the Google Places API Text Search.
 * Each result includes coordinates.
 */
public class PlacesService {

    private static final String SEARCH_URL = "https://places.googleapis.com/v1/places:searchText";
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Searches for places matching text.
     *
     * @param query text entered by user
     * @param maxResults maximum number of places to return
     * @return the matching places
     * @throws IOException if the key is missing or the API returns an error
     */
    public List<Place> search(String query, int maxResults) throws IOException, InterruptedException {
        String apiKey = ApiConfig.getGoogleMapsKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("Google Maps API key not found in config.properties.");
        }

        JSONObject body = new JSONObject()
                .put("textQuery", query)
                .put("pageSize", maxResults)
                .put("regionCode", "AU");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SEARCH_URL))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.location")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Places API returned " + response.statusCode() + ": " + response.body());
        }

        List<Place> results = new ArrayList<>();
        JSONArray places = new JSONObject(response.body()).optJSONArray("places");
        if (places == null) {
            return results;
        }

        for (int i = 0; i < places.length(); i++) {
            JSONObject place = places.getJSONObject(i);
            JSONObject location = place.optJSONObject("location");
            if (location == null) {
                continue;
            }
            JSONObject displayName = place.optJSONObject("displayName");
            String name = displayName != null ? displayName.optString("text", "") : "";
            String address = place.optString("formattedAddress", "");

            results.add(new Place(buildLabel(name, address),
                    location.getDouble("latitude"), location.getDouble("longitude")));
        }
        return results;
    }

    /**
     * Builds the text shown to the user for a place by combining name and address.
     */
    private String buildLabel(String name, String address) {
        if (name.isBlank() || address.startsWith(name)) {
            return address;
        }
        return address.isBlank() ? name : name + ", " + address;
    }
}