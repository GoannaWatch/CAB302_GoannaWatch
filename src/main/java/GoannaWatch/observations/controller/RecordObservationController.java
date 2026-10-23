package GoannaWatch.observations.controller;

import GoannaWatch.account.controller.NavBarController;
import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.observations.model.IObservationDAO;
import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.Place;
import GoannaWatch.observations.model.PlacesService;
import GoannaWatch.observations.model.SqliteObservationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.stream.Collectors;

public class RecordObservationController {
    private final IObservationDAO observationDAO = new SqliteObservationDAO();

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

    @FXML
    public void initialize() {
        navBarController.setPageTitle("Record Observation");
        locationAutocomplete = new LocationAutocomplete(locationTextField, new PlacesService());
        loadSpecies();
        resetForm();
    }

    private void loadSpecies() {
        List<String> species = observationDAO.getAllObservations().stream()
                .map(Observation::getAnimalSeen)
                .filter(s -> !"Unknown".equalsIgnoreCase(s))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toCollection(ArrayList::new));
        species.add(0, "Unknown");
        speciesComboBox.getItems().setAll(species);
    }

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
            observationDAO.addObservation(observation);
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

    @FXML
    private void onClear() {
        statusLabel.setText("");
        resetForm();
    }

    private void resetForm() {
        locationAutocomplete.clear();
        speciesComboBox.getSelectionModel().clearSelection();
        speciesComboBox.getEditor().clear();
        endangeredNoRadio.setSelected(true);
        datePicker.setValue(LocalDate.now());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
