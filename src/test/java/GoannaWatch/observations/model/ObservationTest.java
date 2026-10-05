package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.InputMismatchException;
import static org.junit.jupiter.api.Assertions.*;

class ObservationTest {

    private Account createValidAccount() {
        Account account = new Account(
                "Luke",
                "Smith",
                "luke.smith@example.com",
                "Password1!"
        );

        account.setId(1);
        return account;
    }

    @Test
    void validObservationShouldBeCreated() {
        Account account = createValidAccount();
        LocalDate date = LocalDate.of(2026, 9, 16);

        Observation observation = new Observation (
                account,
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                date
        );

        assertEquals(account, observation.getObserver());
        assertEquals("Brisbane", observation.getLocation());
        assertEquals("Eastern Water Dragon", observation.getAnimalSeen());
        assertEquals(date, observation.getObservedAt());
    }

    @Test
    void nullObserverShouldBeRejected() {
        assertThrows(InputMismatchException.class, () ->
                new Observation(
                        null,
                        "Brisbane",
                        "Eastern Water Dragon",
                        "No",
                        LocalDate.of(2026, 9, 16)
                )
        );
    }

    @Test
    void blankLocationShouldBeRejected() {
        Account account = createValidAccount();

        assertThrows(InputMismatchException.class, () ->
                new Observation(
                        account,
                        "",
                        "Eastern Water Dragon",
                        "No",
                        LocalDate.of(2026, 9, 16)
                )
        );
    }

    @Test
    void nullLocationShouldBeRejected() {
        Account account = createValidAccount();

        assertThrows(InputMismatchException.class, () ->
                new Observation(
                        account,
                        null,
                        "Eastern Water Dragon",
                        "No",
                        LocalDate.of(2026, 9, 16)
                )
        );
    }

    @Test
    void blankAnimalSeenShouldBeRejected() {
        Account account = createValidAccount();

        assertThrows(InputMismatchException.class, () ->
                new Observation(
                        account,
                        "Brisbane",
                        "",
                        "No",
                        LocalDate.of(2026, 9, 16)
                )
        );
    }

    @Test
    void nullAnimalSeenShouldBeRejected() {
        Account account = createValidAccount();

        assertThrows(InputMismatchException.class, () ->
                new Observation(
                        account,
                        "Brisbane",
                        null,
                        "No",
                LocalDate.of(2026, 9, 16)
                )
        );
    }

    @Test
    void nullObservationDateShouldBeRejected() {
        Account account = createValidAccount();

        assertThrows(InputMismatchException.class, () ->
                new Observation(
                        account,
                        "Brisbane",
                        "Eastern Water Dragon",
                        "No",
                        null
                )
        );
    }

    @Test
    void observationLocationShouldBeUpdated() {
        Observation observation = new Observation(
                createValidAccount(),
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                LocalDate.of(2026, 9, 16)
        );

        observation.setPlace(new Place("Gold Coast", -27.977, 153.38));

        assertEquals("Gold Coast", observation.getLocation());
        assertEquals(-27.977, observation.getLatitude());
        assertEquals(153.38, observation.getLongitude());
        assertTrue(observation.hasCoordinates());
    }

    @Test
    void observedAnimalShouldBeUpdated() {
        Observation observation = new Observation(
                createValidAccount(),
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                LocalDate.of(2026, 9, 16)
        );

        observation.setAnimalSeen("Koala");

        assertEquals("Koala", observation.getAnimalSeen());
    }

    @Test
    void observationDateShouldBeUpdated() {
        Observation observation = new Observation(
                createValidAccount(),
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                LocalDate.of(2026, 9, 16)
        );

        LocalDate newDate = LocalDate.of(2026, 9, 15);

        observation.setObservedAt(newDate);

        assertEquals(newDate, observation.getObservedAt());
    }

    @Test
    void observationObserverShouldBeUpdated() {
        Account originalAccount = createValidAccount();

        Account secondAccount = new Account(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "Password1!"
        );

        secondAccount.setId(2);

        Observation observation = new Observation(
                originalAccount,
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                LocalDate.of(2026, 9, 16)
        );

        observation.setObserver(secondAccount);

        assertEquals(secondAccount, observation.getObserver());
    }

    @Test
    void observationIdShouldBeSet() {
        Observation observation = new Observation(
                createValidAccount(),
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                LocalDate.of(2026, 9, 16)
        );

        observation.setId(42);

        assertEquals(42, observation.getId());
    }

}