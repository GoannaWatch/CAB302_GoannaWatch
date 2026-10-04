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
 * Adds place autocomplete to a TextField. As the user types, matching places are
 * fetched (debounced, off the FX thread) and shown in a dropdown. Choosing one
 * records it as the selected Place; typing again invalidates that selection.
 */
public class LocationAutocomplete {

    private static final int MIN_QUERY_LENGTH = 3;
    private static final int MAX_SUGGESTIONS = 5;

    private final TextField textField;
    private final PlacesService placesService;
    private final ContextMenu suggestionMenu = new ContextMenu();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(400));

    private Place selectedPlace;
    private boolean updatingProgrammatically = false;

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

    /** @return the place the user picked from the suggestions, if any */
    public Optional<Place> getSelectedPlace() {
        return Optional.ofNullable(selectedPlace);
    }

    /** Shows an already-resolved place (e.g. when loading a saved observation) without searching. */
    public void setPlace(Place place) {
        setTextQuietly(place.getName());
        selectedPlace = place;
    }

    /** Shows plain text with no resolved place, e.g. a legacy location the user must re-pick. */
    public void setText(String text) {
        setTextQuietly(text);
        selectedPlace = null;
    }

    public void clear() {
        setText("");
    }

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

    private void onTextChanged(String text) {
        if (updatingProgrammatically) {
            return;
        }
        selectedPlace = null; // the user edited the text, so any earlier pick no longer matches it

        String query = text == null ? "" : text.trim();
        if (query.length() < MIN_QUERY_LENGTH) {
            debounce.stop();
            suggestionMenu.hide();
            return;
        }
        debounce.setOnFinished(e -> fetchSuggestions(query));
        debounce.playFromStart();
    }

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