package GoannaWatch.observations.controller;

import GoannaWatch.account.model.Account;
import GoannaWatch.observations.model.Observation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class HistoryControllerTest {

    private Observation createObservation() {
        Account account = new Account(
                "Luke",
                "Smith",
                "luke@example.com",
                "Password1!"
        );

        account.setId(1);

        return new Observation(
                account,
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                LocalDate.of(2026, 9, 16)
        );
    }

    @Test
    void observationShouldMatchLocationSearch() {
        HistoryController controller = new HistoryController();
        Observation observation = createObservation();

        assertTrue(controller.matchesSearch(observation, "Brisbane"));
    }

    @Test
    void locationSearchShouldIgnoreCase() {
        HistoryController controller = new HistoryController();
        Observation observation = createObservation();

        assertTrue(controller.matchesSearch(observation, "brisbane"));
    }

    @Test
    void observationShouldMatchAnimalSearch() {
        HistoryController controller = new HistoryController();
        Observation observation = createObservation();

        assertTrue(controller.matchesSearch(observation, "Water Dragon"));
    }

    @Test
    void animalSearchShouldIgnoreCase() {
        HistoryController controller = new HistoryController();
        Observation observation = createObservation();

        assertTrue(controller.matchesSearch(observation, "eastern water dragon"));
    }

    @Test
    void nonMatchingSearchShouldReturnFalse() {
        HistoryController controller = new HistoryController();
        Observation observation = createObservation();

        assertFalse(controller.matchesSearch(observation, "Gold Coast"));
    }

    @Test
    void blankSearchShouldMatchObservation() {
        HistoryController controller = new HistoryController();
        Observation observation = createObservation();

        assertTrue(controller.matchesSearch(observation, ""));
    }

    @Test
    void nullSearchShouldMatchObservation() {
        HistoryController controller = new HistoryController();
        Observation observation = createObservation();

        assertTrue(controller.matchesSearch(observation, null));
    }












}