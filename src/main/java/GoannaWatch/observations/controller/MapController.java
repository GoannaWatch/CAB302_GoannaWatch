package GoannaWatch.observations.controller;

import GoannaWatch.App;
import GoannaWatch.account.controller.NavBarController;
import javafx.fxml.FXML;

import java.io.IOException;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import GoannaWatch.observations.model.IObservationDAO;
import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.SqliteObservationDAO;
import netscape.javascript.JSObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Controller for the map view of the application.
 */
public class MapController {

    /**
     * Embedded browser displaying Leaflet map.
     */
    @FXML
    private WebView mapWebView;

    @FXML
    private NavBarController navBarController;

    private final IObservationDAO observationDAO;

    public MapController() {
        this(new SqliteObservationDAO());
    }

    MapController(IObservationDAO observationDAO) {
        this.observationDAO = observationDAO;
    }

    /**
     * Initialises the controller class. This method is automatically called after the fxml file has been loaded.
     */
    @FXML
    public void initialize() {
        navBarController.setPageTitle("Map");
        navBarController.setActivePage("Map");
        loadMap();
    }

    /**
     * Loads map.html into the WebView, and adds a pin for every observation with coords.
     */
    private void loadMap() {
        try (InputStream in = App.class.getResourceAsStream("map.html")) {
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            WebEngine engine = mapWebView.getEngine();
            engine.setOnAlert(e -> System.err.println("Map: " + e.getData()));
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    addObservationPins(engine);
                }
            });
            engine.setUserAgent("GoannaWatch");
            engine.loadContent(html);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load the map page.").show();
        }
    }

    /**
     * Adds a pin to the map for  each stored observation.
     * Observations without coords are skipped.
     *
     * @param engine the web engine displaying the loaded page.
     */
    private void addObservationPins(WebEngine engine) {
        JSObject window = (JSObject) engine.executeScript("window");

        for (Observation o : observationDAO.getAllObservations()) {
            if (!o.hasCoordinates()) {
                continue;
            }
            window.call("addObservation",
                    o.getAnimalSeen(),
                    o.getLocation(),
                    o.getObserver().getFullName(),
                    o.getObservedAt().toString(),
                    o.getLatitude(),
                    o.getLongitude());
        }
    }
}
