package GoannaWatch.observations.controller;

import GoannaWatch.observations.model.Place;
import GoannaWatch.observations.model.PlacesService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Adds place autocomplete to a TextField.
 */
public class LocationAutocomplete {

    private static final int MIN_QUERY_LENGTH = 3;
    private static final int MAX_SUGGESTIONS = 5;

    private final TextField textField;


    private final PlacesService placesService;
    private final ContextMenu suggestionMenu = new ContextMenu();

    /**
     * Time delay for lookup until user has paused typing.
     */
    private final PauseTransition debounce = new PauseTransition(Duration.millis(400));

    /**
     * The place the user selected from suggestions, or null.
     */
    private Place selectedPlace;

    /**
     * Is set to true when the class is updating the field's text itself.
     * Means that the listener can differentiate from user typing.
     */
    private boolean updatingProgrammatically = false;

    /**
     * Constructor for LocationAutocomplete.
     * @param textField the field the user types a location into.
     * @param placesService the service used to lookup matching places.
     */
    public LocationAutocomplete(TextField textField, PlacesService placesService) {
        this.textField = textField;
        this.placesService = placesService;

        textField.textProperty().addListener((obs, oldText, newText) -> onTextChanged(newText));
        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                suggestionMenu.hide();
            }
        });
    }

    public Optional<Place> getSelectedPlace() {
        return Optional.ofNullable(selectedPlace);
    }

    public void setPlace(Place place) {
        setTextQuietly(place.getName());
        selectedPlace = place;
    }

    public void setText(String text) {
        setTextQuietly(text);
        selectedPlace = null;
    }

    /**
     * Empties the text field.
     */
    public void clear() {
        setText("");
    }

    /**
     * Sets field's text without starting a lookup.
     * @param text The text to be entered.
     */
    private void setTextQuietly(String text) {
        debounce.stop();
        suggestionMenu.hide();
        updatingProgrammatically = true;
        try {
            textField.setText(text);
        } finally {
            updatingProgrammatically = false;
        }
    }

    /**
     * Handles when a user edits the text field.
     * Invalidates current selected place, hides dropdown, and restarts debounce timer.
     * @param text The new text in the field.
     */
    private void onTextChanged(String text) {
        if (updatingProgrammatically) {
            return;
        }
        selectedPlace = null;

        String query = text == null ? "" : text.trim();
        if (query.length() < MIN_QUERY_LENGTH) {
            debounce.stop();
            suggestionMenu.hide();
            return;
        }
        debounce.setOnFinished(e -> fetchSuggestions(query));
        debounce.playFromStart();
    }

    /**
     * Looks up places match query.
     * Response is dropped if field loses focus or text has changed.
     * @param query the text to search for
     */
    private void fetchSuggestions(String query) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return placesService.search(query, MAX_SUGGESTIONS);
            } catch (Exception e) {
                System.err.println("Place search failed: " + e.getMessage());
                return List.<Place>of();
            }
        }).thenAccept(results -> Platform.runLater(() -> {
            // Drop responses that arrive after the user has typed something else or left the field.
            if (!textField.isFocused() || !query.equals(textField.getText().trim())) {
                return;
            }
            showSuggestions(results);
        }));
    }

    /**
     * Fills dropdown with fetched places.
     * @param results the suggested places
     */
    private void showSuggestions(List<Place> results) {
        suggestionMenu.getItems().clear();
        if (results.isEmpty()) {
            suggestionMenu.hide();
            return;
        }
        for (Place place : results) {
            MenuItem item = new MenuItem(place.getName());
            item.setOnAction(e -> setPlace(place));
            suggestionMenu.getItems().add(item);
        }
        if (!suggestionMenu.isShowing()) {
            suggestionMenu.show(textField, Side.BOTTOM, 0, 0);
        }
    }
}