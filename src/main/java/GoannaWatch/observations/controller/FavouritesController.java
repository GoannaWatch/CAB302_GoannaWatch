package GoannaWatch.observations.controller;

import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.SqliteObservationDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class FavouritesController {

    private final SqliteObservationDAO observationDAO;

    @FXML
    private TableView<Observation> favouritesTableView;

    @FXML
    private TableColumn<Observation, String> observerColumn;

    @FXML
    private TableColumn<Observation, String> locationColumn;

    @FXML
    private TableColumn<Observation, String> animalColumn;

    @FXML
    private TableColumn<Observation, String> dateColumn;

    @FXML
    private TableColumn<Observation, String> endangerColumn;

    private final ObservableList<Observation> favouriteObservations =
            FXCollections.observableArrayList();

    public FavouritesController() {
        observationDAO = new SqliteObservationDAO();
    }

    @FXML
    public void initialize() {

        observerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getObserver().getFullName()));

        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getLocation()));

        animalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getAnimalSeen()));

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getObservedAt().toString()));

        endangerColumn.setCellValueFactory( cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getIsEndangered().toString()));

        favouriteObservations.setAll(
                observationDAO.getAllObservations()
                        .stream()
                        .filter(observation -> observation.getIsFavourite())
                        .toList()
        );

        favouritesTableView.setItems(favouriteObservations);
    }
}
