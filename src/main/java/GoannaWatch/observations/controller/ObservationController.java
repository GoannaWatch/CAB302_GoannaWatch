package GoannaWatch.observations.controller;

import GoannaWatch.App;
import GoannaWatch.account.model.*;
import GoannaWatch.observations.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * The controller class for the Observation view of the GoannaWatch application. This class handles the user interactions in the Observation view.
 */
public class ObservationController {

    private final SqliteObservationDAO observationDAO;

    private LocationAutocomplete locationAutocomplete;

    @FXML
    private TableView<Observation> observationsTableView;

    @FXML
    private TableColumn<Observation, String> observerColumn;

    @FXML
    private TableColumn<Observation, String> locationColumn;

    @FXML
    private TableColumn<Observation, String> animalColumn;

    @FXML
    private TableColumn<Observation, String> endangerColumn;

    @FXML
    private TableColumn<Observation, String> dateColumn;

    @FXML
    private CheckBox showMineOnlyCheck;

    @FXML
    private TextField locationTextField;

    @FXML
    private ComboBox<String> speciesComboBox;

    @FXML
    private RadioButton endangeredYesRadio;

    @FXML
    private RadioButton endangeredNoRadio;

    @FXML
    private ToggleGroup isEndangered;

    @FXML
    private DatePicker datePicker;

    @FXML
    private VBox observationContainer;

    @FXML
    private TextField searchTextField;

    private final ObservableList<Observation> masterObservations = FXCollections.observableArrayList();

    private FilteredList<Observation> filteredObservations;

    private SortedList<Observation> sortedObservations;

    /**
     * Initialises the controller class. This method is automatically called after the .fxml file has been loaded.
     */

    //TODO Create Notification popups using AtlantaFX for edit, delete, and Add, for action confirmation.
    public ObservationController() {
        observationDAO = new SqliteObservationDAO();
    }

    /**
     * Selects a contact in the list view and updates the text fields with the observation's information
     * @param observation The observation to select.
     */
    private void selectObservation(Observation observation) {
        if (observation == null) {
            observationContainer.setVisible(false);
            return;
        }

        observationContainer.setVisible(true);

        if (observation.hasCoordinates()) {
            locationAutocomplete.setPlace(new Place (
                    observation.getLocation(),
                    observation.getLatitude(),
                    observation.getLongitude()));
        } else {
            locationAutocomplete.setText(observation.getLocation());
        }

        speciesComboBox.setValue(observation.getAnimalSeen());

        if ("Yes".equalsIgnoreCase(observation.getIsEndangered())) {
            endangeredYesRadio.setSelected(true);
        } else {
            endangeredNoRadio.setSelected(true);
        }

        datePicker.setValue(observation.getObservedAt());
    }

    /**
     * Initialises the observation controller. Sets up filtering, sorting, and a listener on the search field.
     */
    @FXML
    public void initialize() {
        observerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getObserver().getFullName()));
        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLocation()));
        animalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAnimalSeen()));
        endangerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIsEndangered()));
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getObservedAt().toString()));

        locationAutocomplete = new LocationAutocomplete(locationTextField, new PlacesService());

        filteredObservations = new FilteredList<>(masterObservations, o -> true);
        sortedObservations = new SortedList<>(filteredObservations);
        sortedObservations.comparatorProperty().bind(observationsTableView.comparatorProperty());
        observationsTableView.setItems(sortedObservations);

        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        observationsTableView.getSortOrder().add(dateColumn);

        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        observationsTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectObservation(newSelection));

        loadObservationsFromDao();

        updateSpeciesComboBox();
        speciesComboBox.setEditable(true);

        observationsTableView.getItems().addListener((ListChangeListener<Observation>) change -> {
            updateSpeciesComboBox();
        });

        observationsTableView.getSelectionModel().selectFirst();
    }

    /**
     * Loads observations from DAO. If ShowMine checkbox toggled, it will on load observations created by current user.
     */
    private void loadObservationsFromDao() {
        List<Observation> observations;
        if (showMineOnlyCheck.isSelected()) {
            Account currentAccount = Session.getCurrentAccount();
            observations = (currentAccount != null)
                    ? observationDAO.getObservationsByAccount(currentAccount)
                    : List.of();
        } else {
            observations = observationDAO.getAllObservations();
        }
        masterObservations.setAll(observations);
        observationContainer.setVisible(!observations.isEmpty());
    }

    /**
     * Handles the "show only mine" checkbox. Reloads observations from DAO.
     */
    @FXML
    private void onFilterChanged() {
        loadObservationsFromDao();
        applyFilter();
    }

    //Checks whether an observation matches the supplied search query by animal name or location.
    //Package-private so the filtering behaviour can be unit tested without requiring JavaFX controls to be initialised.
    boolean matchesSearch(Observation observation, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String lowerQuery = query.toLowerCase();

        return observation.getAnimalSeen().toLowerCase().contains(lowerQuery) ||
                observation.getLocation().toLowerCase().contains(lowerQuery);
    }

    /**
     * Filters objects based on searchTextField for animal or location.
     * // TODO Extend so that filters Account and Date as well.
     */
    private void applyFilter() {
        String query = searchTextField.getText();

        filteredObservations.setPredicate(
                observation -> matchesSearch(observation, query)
        );
    }

    /**
     * Saves changes made to currently selected observation.
     */
    @FXML
    private void onEditConfirm() {
        Observation selected = observationsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            Place place = locationAutocomplete.getSelectedPlace().orElse(null);
            if (place == null) {
                showAlert("Please choose a location from the suggestions.");
                return;
            }

            selected.setPlace(place);
            selected.setAnimalSeen(speciesComboBox.getValue());

            RadioButton selectedEndangered = (RadioButton) isEndangered.getSelectedToggle();

            if (selectedEndangered != null) {
                selected.setIsEndangered(selectedEndangered.getText());
            }

            selected.setObservedAt(datePicker.getValue());

            observationDAO.updateObservation(selected);
            int id = selected.getId();
            loadObservationsFromDao();
            selectById(id);

        } catch (InputMismatchException e) {
            showAlert(e.getMessage());
        }
    }

    /**
     * Deletes the currently selected observation.
     */
    @FXML
    private void onDelete() {
        Observation selected = observationsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            observationDAO.deleteObservation(selected);
            loadObservationsFromDao();
        }
    }

    /**
     * Creates a new observation with default placeholder values.
     */
    @FXML
    private void onAdd() {
        Account currentAccount = Session.getCurrentAccount();
        if (currentAccount == null) {
            showAlert("You must be logged in to record an observation.");
            return;
        }

        final String DEFAULT_LOCATION = "New Location";
        final String DEFAULT_ANIMAL = "Unknown";
        final String DEFAULT_STATUS = "No";
        final LocalDate DEFAULT_DATE = LocalDate.now();

        Observation newObservation = new Observation(
                currentAccount,
                DEFAULT_LOCATION,
                null,
                null,
                DEFAULT_ANIMAL,
                DEFAULT_STATUS,
                DEFAULT_DATE
        );

        observationDAO.addObservation(newObservation);
        loadObservationsFromDao();
        selectById(newObservation.getId());
        locationTextField.requestFocus();
    }

    /**
     * Selects the table row for the observation with the given ID.
     * Fixes reload from Sqlite changing the selected row on refresh by creating a reference.
     * @param id the database ID of the observation to select
     */
    private void selectById(int id) {
        sortedObservations.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .ifPresent(o -> observationsTableView.getSelectionModel().select(o));
    }

    /**
     * Discards any unsaved changes in the detail pane.
     */
    @FXML
    private void onCancel() {
        Observation selected = observationsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectObservation(selected);
        }
    }

    /**
     * Handles the action of clicking the back button. Loads the landing view of the application.
     * @throws IOException If the .fxml file for the landing view isn't found.
     */
    @FXML
    private void onBackButtonClick() throws IOException {
        Stage stage = (Stage) observationsTableView.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("landing.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    /**
     * Displays an alert dialog box to the user.
     * @param message The text shown in the body of the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Updates species combo box to include all species previously inputted
     */
    private void updateSpeciesComboBox(){
        speciesComboBox.getItems().setAll(); // empty list
        String[] habitatsList = observationsTableView.getItems().stream().map(Observation::getAnimalSeen).distinct().toArray(String[]::new);
        speciesComboBox.getItems().addAll(habitatsList); // add the current distinct locations from the table
        speciesComboBox.getItems().removeAll("Unknown");
        speciesComboBox.getItems().addFirst("Unknown"); // since unknown can be a value in the table, but is also a default value given in the combobox, we reset it here to prevent duplicates
    }

    /**
     * Exports Observation table to csv file
     */
    @FXML
    private void onExport() {
        observationDAO.exportObservations();
    }
}
