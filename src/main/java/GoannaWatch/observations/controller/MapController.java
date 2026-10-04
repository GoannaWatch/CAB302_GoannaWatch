package GoannaWatch.observations.controller;

import GoannaWatch.App;
import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.config.ApiConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import GoannaWatch.observations.model.IObservationDAO;
import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.SqliteObservationDAO;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class MapController {

    @FXML
    private Hyperlink logoutButton;

    @FXML
    private Button newObservationButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button mapButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Hyperlink themeButton;

    @FXML
    private WebView mapWebView;

    private final IObservationDAO observationDAO = new SqliteObservationDAO();

    /**
     * Initialises the controller class. This method is automatically called after the fxml file has been loaded.
     */
    @FXML
    public void initialize(){
        Account current = Session.getCurrentAccount();
        updateThemeButtonText();
        loadMap();
    }

    /**
     * Updates the theme button text to show the theme the user can switch to
     */
    private void updateThemeButtonText() {
        if (App.isDarkMode()) {
            themeButton.setText("Light Mode");
        } else {
            themeButton.setText("Dark Mode");
        }
    }

    /**
     * Switches the application between dark mode and light mode
     */
    @FXML
    private void onThemeButtonClick() {
        App.toggleTheme();
        updateThemeButtonText();
    }

    /**
     * Handles the action of clicking the logout button. Loads the Welcome page view of the application.
     * @throws IOException If the .fxml file for the welcome view isn't found.
     */
    @FXML
    protected void onLogoutButtonClick() throws IOException {
        Session.clear();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("welcome.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    /**
     * Handles the action of clicking the "Record an Observation" button. Loads the Observation view of the application.
     * @throws IOException If the .fxml file for the observation view isn't found.
     */
    @FXML
    protected void onNewObservationButtonClick() throws IOException {
        Stage stage = (Stage) newObservationButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("observation.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    //TODO Create history page
    protected void onHistoryButtonClick() throws IOException {
        Stage stage = (Stage) historyButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("history.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    //TODO create Map page
    protected void onMapButtonClick() throws IOException {
    //    Stage stage = (Stage) mapButton.getScene().getWindow();
    //    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("map.fxml"));
    //    Scene scene = new Scene(fxmlLoader.load());
    //    stage.setScene(scene);
    }

    @FXML
    //TODO Create Dashboard page
    protected void onDashboardButtonClick() throws IOException {
        Stage stage = (Stage) mapButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("landing.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    public void onProfileButtonClick() {
    }

    /**
     * Loads map.html into the WebView, injecting the API key, then drops a pin
     * for every observation once the page has loaded.
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
     *
     * @param engine
     */
    private void addObservationPins(WebEngine engine) {
        for (Observation o : observationDAO.getAllObservations()) {
            if (!o.hasCoordinates()) {
                continue;
            }
            engine.executeScript("addObservation(" + toJson(o) + ")");
        }
    }

    private String toJson(Observation o) {
        return "{\"animal\":" + jsString(o.getAnimalSeen())
                + ",\"location\":" + jsString(o.getLocation())
                + ",\"observer\":" + jsString(o.getObserver().getFullName())
                + ",\"date\":" + jsString(o.getObservedAt().toString())
                + ",\"lat\":" + o.getLatitude()
                + ",\"lng\":" + o.getLongitude() + "}";
    }

    /** Wraps text as a safely escaped JavaScript string literal. */
    private String jsString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("<", "\\u003c") + "\"";
    }
}
