package GoannaWatch.observations.controller;

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

/**
 * Controller  for favourites page. Lists the observations the current account has favourited.
 */
public class FavouritesController {

    private final ObservationService service;
    private final ObservableList<Observation> favouriteObservations = FXCollections.observableArrayList();

    @FXML private NavBarController navBarController;
    @FXML private TableView<Observation> favouritesTableView;
    @FXML private TableColumn<Observation, String> observerColumn;
    @FXML private TableColumn<Observation, String> locationColumn;
    @FXML private TableColumn<Observation, String> animalColumn;
    @FXML private TableColumn<Observation, String> endangerColumn;
    @FXML private TableColumn<Observation, String> dateColumn;
    @FXML private TableColumn<Observation, Boolean> favouriteColumn;

    /**
     * Creates a controller for favourites of the user. Backed by SQLite.
     */
    public FavouritesController() {
        this(new ObservationService(
                new SqliteObservationDAO(),
                new SqliteCommentDAO(),
                new SqliteFavouriteDAO()));
    }

    /**
     * Package-private for testing.
     */
    FavouritesController(ObservationService service) {
        this.service = service;
    }

    /**
     * Initialises the navbar and table, then loads the favourite observations.
     */
    @FXML
    public void initialize() {

        navBarController.setPageTitle("Favourites");
        navBarController.setActivePage("Favourites");

        observerColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getObserver().getFullName()));
        locationColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getLocation()));
        animalColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getAnimalSeen()));
        endangerColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getIsEndangered()));
        dateColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getObservedAt().toString()));
        favouriteColumn.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().getIsFavourite()));

        favouriteColumn.setCellFactory(col ->
                new FavouriteStarCell(service, o -> {
                    if (!o.getIsFavourite()) {
                        favouriteObservations.remove(o);
                    }
                }));

        favouritesTableView.setPlaceholder(
                new Label("No favourites yet. Click the star on an observation to add one."));
        favouritesTableView.setItems(favouriteObservations);

        favouriteObservations.setAll(service.getFavourites(Session.getCurrentAccount()).stream()
                .sorted(Comparator.comparing(Observation::getObservedAt).reversed())
                .toList());
    }
}
