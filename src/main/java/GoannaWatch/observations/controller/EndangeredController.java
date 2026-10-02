package GoannaWatch.observations.controller;

import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.SqliteObservationDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class EndangeredController {

    private final SqliteObservationDAO observationDAO;

    @FXML
    private TableView<Observation> endangeredTableView;

    @FXML
    private TableColumn<Observation, String> observerColumn;

    @FXML
    private TableColumn<Observation, String> locationColumn;

    @FXML
    private TableColumn<Observation, String> animalColumn;

    @FXML
    private TableColumn<Observation, String> dateColumn;

    private final ObservableList<Observation> endangeredObservations =
            FXCollections.observableArrayList();

    public EndangeredController() {
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

        endangeredObservations.setAll(
                observationDAO.getAllObservations()
                        .stream()
                        .filter(observation ->
                                "Yes".equalsIgnoreCase(
                                        observation.getIsEndangered()))
                        .toList()
        );

        endangeredTableView.setItems(endangeredObservations);
    }
}
