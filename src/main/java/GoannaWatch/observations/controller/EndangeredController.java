package GoannaWatch.observations.controller;

import GoannaWatch.App;
import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.SqliteObservationDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

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

    @FXML
    private TableColumn<Observation, Boolean> favouriteColumn;

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

        favouriteColumn.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(
                        cellData.getValue().getIsFavourite()));
    }
    @FXML
    private void onBackButtonClick() throws IOException {
        Stage stage = (Stage) endangeredTableView.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("landing.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }
}
