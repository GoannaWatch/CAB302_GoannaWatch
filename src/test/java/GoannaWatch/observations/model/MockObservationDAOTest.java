package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MockObservationDAOTest {

    private Account createValidAccount(int id) {
        Account account = new Account(
                "Luke",
                "Smith",
                "luke" + id + "@example.com",
                "Password1!"
        );

        account.setId(id);
        return account;
    }

    private Observation createValidObservation(Account account) {
        return new Observation(
                account,
                "Brisbane",
                "Eastern Water Dragon",
                "No",
                LocalDate.of(2026, 9, 16)
        );
    }

    @Test
    void addedObservationShouldBeRetrievable() {
        MockObservationDAO dao = new MockObservationDAO();

        Account account = createValidAccount(1);
        Observation observation = createValidObservation(account);

        dao.addObservation(observation);

        Observation retrievedObservation =
                dao.getObservation(observation.getId());

        assertNotNull(retrievedObservation);
        assertEquals(observation.getId(), retrievedObservation.getId());
        assertEquals(account, retrievedObservation.getObserver());
        assertEquals("Brisbane", retrievedObservation.getLocation());
        assertEquals("Eastern Water Dragon", retrievedObservation.getAnimalSeen());
    }

    @Test
    void addedObservationShouldReceiveAnId() {
        MockObservationDAO dao = new MockObservationDAO();

        Account account = createValidAccount(1);
        Observation firstObservation = createValidObservation(account);

        Observation secondObservation = new Observation(
                account,
                "Gold Coast",
                "Koala",
                "No",
                LocalDate.of(2026, 9, 15)
        );

        dao.addObservation(firstObservation);
        dao.addObservation(secondObservation);

        assertNotEquals(firstObservation.getId(), secondObservation.getId());
    }

    @Test
    void allObservationsShouldBeReturned() {
        MockObservationDAO dao = new MockObservationDAO();

        Account account = createValidAccount(1);

        Observation firstObservation = createValidObservation(account);
        Observation secondObservation = new Observation(
                account,
                "Gold Coast",
                "Koala",
                "No",
                LocalDate.of(2026, 9, 15)
        );

        dao.addObservation(firstObservation);
        dao.addObservation(secondObservation);

        List<Observation> observations = dao.getAllObservations();

        assertNotNull(observations);
        assertTrue(observations.contains(firstObservation));
        assertTrue(observations.contains(secondObservation));
    }

    @Test
    void observationShouldBeUpdated() {
        MockObservationDAO dao = new MockObservationDAO();

        Account account = createValidAccount(1);
        Observation observation = createValidObservation(account);

        dao.addObservation(observation);

        observation.setPlace(new Place("Gold Coast", -27.977, 153.38));
        observation.setAnimalSeen("Koala");

        dao.updateObservation(observation);

        Observation updatedObservation = dao.getObservation(observation.getId());

        assertNotNull(updatedObservation);
        assertEquals("Gold Coast", updatedObservation.getLocation());
        assertEquals(-27.977, updatedObservation.getLatitude());
        assertEquals(153.38, updatedObservation.getLongitude());
        assertEquals("Koala", updatedObservation.getAnimalSeen());
    }

    @Test
    void observationShouldBeDeleted() {
        MockObservationDAO dao = new MockObservationDAO();

        Account account = createValidAccount(1);
        Observation observation = createValidObservation(account);

        dao.addObservation(observation);

        int observationId = observation.getId();

        dao.deleteObservation(observation);

        assertNull(dao.getObservation(observationId));
    }

    @Test
    void requestingUnknownObservationShouldReturnNull() {
        MockObservationDAO dao = new MockObservationDAO();

        Observation observation = dao.getObservation(Integer.MAX_VALUE);

        assertNull(observation);
    }

    @Test
    void returnedObservationListShouldNotExposeInternalList() {
        MockObservationDAO dao = new MockObservationDAO();

        int originalSize = dao.getAllObservations().size();

        List<Observation> returnedObservations = dao.getAllObservations();

        returnedObservations.clear();

        assertEquals(originalSize, dao.getAllObservations().size());
    }

    @Test
    void observationsShouldbeFilteredByAccount() {
        MockObservationDAO dao = new MockObservationDAO();

        Account firstAccount = createValidAccount(1);
        Account secondAccount = createValidAccount(2);

        Observation firstObservation = createValidObservation(firstAccount);

        Observation secondObservation = new Observation(
                secondAccount,
                "Gold Coast",
                "Koala",
                "No",
                LocalDate.of(2026, 9, 15)
        );

        dao.addObservation(firstObservation);
        dao.addObservation(secondObservation);

        List<Observation> firstAccountObservations = dao.getObservationsByAccount(firstAccount);

        assertTrue(firstAccountObservations.contains(firstObservation));
        assertFalse(firstAccountObservations.contains(secondObservation));
    }

    @Test
    void accountWithNoObservationsShouldReturnEmptyList() {
        MockObservationDAO dao = new MockObservationDAO();

        Account accountWithNoObservations = createValidAccount(999999);

        List<Observation> observations = dao.getObservationsByAccount(accountWithNoObservations);

        assertNotNull(observations);
        assertTrue(observations.isEmpty());
    }

}