package GoannaWatch.observations.controller;

import GoannaWatch.App;
import GoannaWatch.account.controller.NavBarController;
import GoannaWatch.account.model.Session;
import GoannaWatch.observations.model.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class EndangeredController {

    private final ObservationService service;
    private final ObservableList<Observation> endangeredObservations = FXCollections.observableArrayList();

    @FXML private NavBarController navBarController;
    @FXML private TableView<Observation> endangeredTableView;
    @FXML private TableColumn<Observation, String> observerColumn;
    @FXML private TableColumn<Observation, String> locationColumn;
    @FXML private TableColumn<Observation, String> animalColumn;
    @FXML private TableColumn<Observation, String> dateColumn;
    @FXML private TableColumn<Observation, Boolean> favouriteColumn;

    public EndangeredController() {
        this(new ObservationService(
                new SqliteObservationDAO(),
                new SqliteCommentDAO(),
                new SqliteFavouriteDAO()));
    }

    EndangeredController(ObservationService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {

        navBarController.setPageTitle("Endangered");
        navBarController.setActivePage("Endangered");

        observerColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getObserver().getFullName()));
        locationColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getLocation()));
        animalColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getAnimalSeen()));
        dateColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getObservedAt().toString()));
        favouriteColumn.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().getIsFavourite()));
        favouriteColumn.setCellFactory(col -> new FavouriteStarCell(service, o -> { }));

        endangeredTableView.setPlaceholder(new Label("No endangered animals have been recorded yet."));
        endangeredTableView.setItems(endangeredObservations);

        endangeredObservations.setAll(service.getEndangered(Session.getCurrentAccount()).stream()
                .sorted(Comparator.comparing(Observation::getObservedAt).reversed())
                .toList());
    }
}
