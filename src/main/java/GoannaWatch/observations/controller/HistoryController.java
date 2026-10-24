package GoannaWatch.observations.controller;

import GoannaWatch.App;
import GoannaWatch.account.controller.NavBarController;
import GoannaWatch.account.model.*;
import GoannaWatch.observations.model.*;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import GoannaWatch.observations.model.Comment;

/**
 * The controller class for the Observation view of the GoannaWatch application. This class handles the user interactions in the Observation view.
 */
public class HistoryController {

    @FXML
    private Label ExportSuccessMsg;

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
    private TableColumn<Observation, Boolean> favouriteColumn;

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

    @FXML
    private Label imageLabel;

    @FXML
    private Label videoLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextArea commentsArea;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private VBox commentForm;

    private final ObservableList<Observation> masterObservations = FXCollections.observableArrayList();

    private FilteredList<Observation> filteredObservations;

    private SortedList<Observation> sortedObservations;

    private File selectedImage;

    private File selectedVideo;

    @FXML
    private NavBarController navBarController;

    private final ObservationExporter exporter = new ObservationExporter();

    private final ObservationService service;

    /**
     * Initialises the controller class. This method is automatically called after the .fxml file has been loaded.
     */
    public HistoryController() {
        this(new ObservationService(
                new SqliteObservationDAO(),
                new SqliteCommentDAO(),
                new SqliteFavouriteDAO()));
    }

    /**
     * Package-private for testing.
     */
    HistoryController(ObservationService service) {
        this.service = service;
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

        // Only show edit and delete buttons to the owner or an expert.
        Account account = Session.getCurrentAccount();
        boolean allowed = observation.canBeModifiedBy(account);

        confirmButton.setVisible(allowed);
        confirmButton.setManaged(allowed);
        deleteButton.setVisible(allowed);
        deleteButton.setManaged(allowed);
        cancelButton.setVisible(allowed);
        cancelButton.setManaged(allowed);

        // Lock the inputs, so a user viewing someone else's observation can't type into them.
        locationTextField.setDisable(!allowed);
        speciesComboBox.setDisable(!allowed);
        endangeredYesRadio.setDisable(!allowed);
        endangeredNoRadio.setDisable(!allowed);
        datePicker.setDisable(!allowed);

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

        // Only experts can write comments.
        boolean expert = account != null && account.isExpert();
        commentForm.setVisible(expert);
        commentForm.setManaged(expert);

        commentTextArea.clear();
        loadComments(observation.getId());
    }

    /**
     * Displays the comments for the selected observation.
     *
     * @param observationId The ID of the observation.
     */
    private void loadComments(int observationId) {
        commentsArea.clear();

        try {
            List<Comment> comments = service.getComments(observationId);

            for (Comment comment : comments) {
                commentsArea.appendText(
                        comment.getAuthor().getFullName() + ": "
                                + comment.getText() + "\n\n"
                );
            }
        } catch (RuntimeException e) {
            showAlert("Could not load comments.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Adds a comment to the selected observation.
     */
    @FXML
    private void onAddComment() {
        Observation selected = observationsTableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        try {
            service.addComment(
                    selected.getId(),
                    Session.getCurrentAccount(),
                    commentTextArea.getText().trim());
            commentTextArea.clear();
            loadComments(selected.getId());

        } catch (InputMismatchException | SecurityException e) {
            showAlert(e.getMessage());
        } catch (RuntimeException e) {
            showAlert("Could not save comment.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Initialises the observation controller. Sets up filtering, sorting, and a listener on the search field.
     */
    @FXML
    public void initialize() {
        navBarController.setPageTitle("History");
        navBarController.setActivePage("History");
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
        favouriteColumn.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().getIsFavourite()));
        favouriteColumn.setCellFactory(col -> new FavouriteStarCell(service, o -> { }));



        locationAutocomplete = new LocationAutocomplete(locationTextField, new PlaceSearchClient());

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
        Account current = Session.getCurrentAccount();
        List<Observation> observations;
        if (showMineOnlyCheck.isSelected()) {
            observations = (current != null) ? service.getMine(current) : List.of();
        } else {
            observations = service.getAll(current);
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

            service.update(selected, Session.getCurrentAccount());
            int id = selected.getId();
            loadObservationsFromDao();
            selectById(id);

        } catch (InputMismatchException e) {
            showAlert(e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    /**
     * Deletes the currently selected observation.
     */
    @FXML
    private void onDelete() {
        Observation selected = observationsTableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Observation");
        alert.setHeaderText(null);
        alert.setContentText("Delete this observation?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            service.delete(selected, Session.getCurrentAccount());
            loadObservationsFromDao();
        } catch (SecurityException | IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
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
    private void updateSpeciesComboBox() {
        speciesComboBox.getItems().setAll(service.getKnownSpecies());
    }

    /**
     * Exports Observation table to csv file
     */
    @FXML
    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Observations");
        chooser.setInitialFileName("observations.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        File file = chooser.showSaveDialog(observationsTableView.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }

        try {
            List<Observation> rows = new ArrayList<>(sortedObservations);
            exporter.export(rows, file.toPath());
            ExportSuccessMsg.setText("Exported " + rows.size() + " observations");
        } catch (IOException e) {
            showAlert("Could not export: " + e.getMessage());
        }
    }

    @FXML
    private void onChooseImage() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Choose Observation Image");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif"
                )
        );

        Window window = observationsTableView.getScene().getWindow();

        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            selectedImage = file;
            imageLabel.setText(file.getName());
        }
    }

    @FXML
    private void onChooseVideo() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Choose Observation Video");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Video Files",
                        "*.mp4",
                        "*.mov",
                        "*.avi",
                        "*.mkv"
                )
        );

        Window window = observationsTableView.getScene().getWindow();

        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            selectedVideo = file;
            videoLabel.setText(file.getName());
        }
    }
}
