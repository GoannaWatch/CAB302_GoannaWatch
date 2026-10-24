package GoannaWatch.observations.controller;

import GoannaWatch.account.controller.NavBarController;
import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.observations.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Record Observations page.
 */
public class RecordObservationController {
    private LocationAutocomplete locationAutocomplete;

    @FXML
    private NavBarController navBarController;

    @FXML
    private TextField locationTextField;

    @FXML
    private ComboBox<String> speciesComboBox;

    @FXML
    private RadioButton endangeredYesRadio;

    @FXML
    private RadioButton endangeredNoRadio;

    @FXML
    private ToggleGroup endangeredGroup;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label statusLabel;

    private final ObservationService service;

    /**
     * Constructs a controller backed by SQLite database.
     */
    public RecordObservationController() {
        this(new ObservationService(
                new SqliteObservationDAO(),
                new SqliteCommentDAO(),
                new SqliteFavouriteDAO()));
    }

    /**
     * Package-private. Used for testing
     */
    RecordObservationController(ObservationService service) {
        this.service = service;
    }

    /**
     * Initialises navbar, attaches autocomplete to location, fills species list and resets form.
     */
    @FXML
    public void initialize() {
        navBarController.setPageTitle("Record Observation");
        locationAutocomplete = new LocationAutocomplete(locationTextField, new PlaceSearchClient());
        loadSpecies();
        resetForm();
    }

    /**
     * Fills the species drop-down with recorded species.
     */
    private void loadSpecies() {
        speciesComboBox.getItems().setAll(service.getKnownSpecies());
    }

    /**
     * Handles the submit button.
     * Any issues are shown as an alert.
     */
    @FXML
    private void onSubmit() {
        statusLabel.setText("");

        Account account = Session.getCurrentAccount();
        if (account == null) {
            showAlert("You must be logged in to record an observation.");
            return;
        }

        Place place = locationAutocomplete.getSelectedPlace().orElse(null);
        if (place == null) {
            showAlert("Please choose a location from the suggestions.");
            return;
        }

        String species = speciesComboBox.getEditor().getText().trim();

        RadioButton selected = (RadioButton) endangeredGroup.getSelectedToggle();
        String endangered = selected == null ? "No" : selected.getText();

        try {
            Observation observation = new Observation(
                    account,
                    place.getName(),
                    place.getLatitude(),
                    place.getLongitude(),
                    species,
                    endangered,
                    datePicker.getValue(),
                    false);
            service.add(observation, account);
        } catch (InputMismatchException e) {
            showAlert(e.getMessage());
            return;
        } catch (RuntimeException e) {
            showAlert("Could not save the observation.");
            System.err.println(e.getMessage());
            return;
        }

        statusLabel.setText("Observation recorded.");
        loadSpecies();
        resetForm();
    }

    /**
     * Handles the clear button. Empties the status message and resets the form.
     */
    @FXML
    private void onClear() {
        statusLabel.setText("");
        resetForm();
    }

    /**
     * Resets the form to the starting status.
     */
    private void resetForm() {
        locationAutocomplete.clear();
        speciesComboBox.getSelectionModel().clearSelection();
        speciesComboBox.getEditor().clear();
        endangeredNoRadio.setSelected(true);
        datePicker.setValue(LocalDate.now());
    }

    /**
     * Shows a message to the user and waits until closed.
     * @param message the text to display
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
