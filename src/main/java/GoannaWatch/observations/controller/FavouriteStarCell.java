package GoannaWatch.observations.controller;

import GoannaWatch.account.model.Session;
import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.ObservationService;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.util.function.Consumer;

/**
 * Table cell that shows a star button for adding an observation to the current account's favourites.
 */
public class FavouriteStarCell extends TableCell<Observation, Boolean> {

    private final Button button = new Button();
    private final ObservationService service;
    private final Consumer<Observation> onChanged;

    /**
     * Create a star cell
     * @param service used to save the change
     * @param onChanged called after a successful toggle, for example to remove
     *                  the row from a list of favourites
     */
    public FavouriteStarCell(ObservationService service, Consumer<Observation> onChanged) {
        this.service = service;
        this.onChanged = onChanged;
        button.getStyleClass().add("flat");
        button.setStyle("-fx-text-fill: #f5b301; -fx-font-size: 16px;");
        button.setOnAction(e -> toggle());
    }

    /**
     * Flips the favourite state of the row's observation.
     */
    private void toggle() {
        Observation observation = currentObservation();
        if (observation == null) {
            return;
        }
        boolean next = !observation.getIsFavourite();
        try {
            service.setFavourite(observation, Session.getCurrentAccount(), next);
            button.setText(next ? "★" : "☆");
            onChanged.accept(observation);
        } catch (RuntimeException ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Could not update favourite: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Returns the observation shown in the cell's row.
     * @return the row's observation
     */
    private Observation currentObservation() {
        return (isEmpty() || getTableRow() == null) ? null : getTableRow().getItem();
    }

    /**
     * Shows the star for the row's current state, or nothing for an empty row.
     */
    private void refresh() {
        Observation observation = currentObservation();
        if (observation == null) {
            setGraphic(null);
        } else {
            button.setText(observation.getIsFavourite() ? "★" : "☆");
            setGraphic(button);
        }
    }

    @Override
    protected void updateItem(Boolean favourite, boolean empty) {
        super.updateItem(favourite, empty);
        refresh();
    }

    @Override
    public void updateIndex(int index) {
        super.updateIndex(index);
        refresh();
    }
}