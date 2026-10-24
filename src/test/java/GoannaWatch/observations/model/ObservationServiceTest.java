package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.SqliteAccountDAO;
import GoannaWatch.database.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the observation business rules against a real SQLite database.
 */
class ObservationServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);

    @TempDir
    Path folder;

    private String oldUrl;
    private SqliteObservationDAO observationDAO;
    private ObservationService service;
    private Account owner;
    private Account other;
    private Account expert;
    private Observation observation;

    @BeforeEach
    void setUp() throws Exception {
        oldUrl = System.getProperty("goannawatch.db.url");
        System.setProperty("goannawatch.db.url",
                "jdbc:sqlite:" + folder.resolve("service.db").toAbsolutePath());
        DatabaseInitializer.initialize();

        SqliteAccountDAO accountDAO = new SqliteAccountDAO();
        accountDAO.addAccount(new Account("Owner", "Test", "owner@example.com", "Test123!"));
        accountDAO.addAccount(new Account("Other", "Test", "other@example.com", "Test123!"));
        accountDAO.addAccount(new Account("Expert", "Test", "expert@example.com", "Test123!"));

        owner = accountDAO.getAccountByEmail("owner@example.com");
        other = accountDAO.getAccountByEmail("other@example.com");
        accountDAO.grantExpert(accountDAO.getAccountByEmail("expert@example.com").getId());
        expert = accountDAO.getAccountByEmail("expert@example.com");
        assertTrue(expert.isExpert());

        observationDAO = new SqliteObservationDAO();
        service = new ObservationService(
                observationDAO, new SqliteCommentDAO(), new SqliteFavouriteDAO());

        observation = new Observation(owner, "Brisbane", "Koala", "No", DATE);
        observationDAO.addObservation(observation);
    }

    @AfterEach
    void tearDown() {
        if (oldUrl == null) {
            System.clearProperty("goannawatch.db.url");
        } else {
            System.setProperty("goannawatch.db.url", oldUrl);
        }
    }

    private Observation saved() {
        return observationDAO.getObservation(observation.getId());
    }

    private static boolean markedFavourite(List<Observation> list, int id) {
        return list.stream().filter(o -> o.getId() == id).findFirst().orElseThrow().getIsFavourite();
    }

    @Test
    void observerCanAddOwnObservation() {
        Observation fresh = new Observation(owner, "Cairns", "Cassowary", "Yes", DATE);

        service.add(fresh, owner);

        assertTrue(fresh.getId() > 0);
        assertNotNull(observationDAO.getObservation(fresh.getId()));
    }

    @Test
    void anotherAccountCannotAddOnSomeoneElsesBehalf() {
        Observation fresh = new Observation(owner, "Cairns", "Cassowary", "Yes", DATE);

        assertThrows(SecurityException.class, () -> service.add(fresh, other));
        assertEquals(1, service.getAll(owner).size());
    }

    @Test
    void nobodyLoggedInCannotAdd() {
        Observation fresh = new Observation(owner, "Cairns", "Cassowary", "Yes", DATE);

        assertThrows(SecurityException.class, () -> service.add(fresh, null));
    }

    @Test
    void ownerCanUpdateObservation() {
        observation.setAnimalSeen("Wallaby");

        service.update(observation, owner);

        assertEquals("Wallaby", saved().getAnimalSeen());
    }

    @Test
    void expertCanUpdateAnotherUsersObservation() {
        observation.setAnimalSeen("Wallaby");

        service.update(observation, expert);

        assertEquals("Wallaby", saved().getAnimalSeen());
    }

    @Test
    void otherUserCannotUpdateObservation() {
        observation.setAnimalSeen("Wallaby");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.update(observation, other));

        assertEquals("You can only edit your own observations.", ex.getMessage());
        assertEquals("Koala", saved().getAnimalSeen());
    }

    @Test
    void nobodyLoggedInCannotUpdate() {
        observation.setAnimalSeen("Wallaby");

        assertThrows(SecurityException.class, () -> service.update(observation, null));
        assertEquals("Koala", saved().getAnimalSeen());
    }

    @Test
    void ownerCanDeleteObservation() {
        service.delete(observation, owner);

        assertNull(saved());
    }

    @Test
    void expertCanDeleteAnotherUsersObservation() {
        service.delete(observation, expert);

        assertNull(saved());
    }

    @Test
    void otherUserCannotDeleteObservation() {
        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.delete(observation, other));

        assertEquals("You can only delete your own observations.", ex.getMessage());
        assertNotNull(saved());
    }

    @Test
    void getMineReturnsOnlyThatAccountsObservations() {
        Observation othersObservation = new Observation(other, "Perth", "Quokka", "No", DATE);
        observationDAO.addObservation(othersObservation);

        List<Observation> mine = service.getMine(owner);

        assertEquals(1, mine.size());
        assertEquals(observation.getId(), mine.get(0).getId());
    }

    @Test
    void getAllReturnsEveryAccountsObservations() {
        observationDAO.addObservation(new Observation(other, "Perth", "Quokka", "No", DATE));

        assertEquals(2, service.getAll(owner).size());
    }

    @Test
    void getEndangeredReturnsOnlyEndangeredObservations() {
        Observation endangered = new Observation(owner, "Cairns", "Cassowary", "Yes", DATE);
        observationDAO.addObservation(endangered);

        List<Observation> result = service.getEndangered(owner);

        assertEquals(1, result.size());
        assertEquals(endangered.getId(), result.get(0).getId());
    }

    @Test
    void knownSpeciesAreDistinctSortedAndStartWithUnknown() {
        observationDAO.addObservation(new Observation(owner, "Perth", "zebra finch", "No", DATE));
        observationDAO.addObservation(new Observation(owner, "Perth", "Aardvark", "No", DATE));
        observationDAO.addObservation(new Observation(owner, "Perth", "Koala", "No", DATE));
        observationDAO.addObservation(new Observation(owner, "Perth", "Unknown", "No", DATE));

        List<String> species = service.getKnownSpecies();

        assertEquals("Unknown", species.get(0));
        assertEquals(1, Collections.frequency(species, "Unknown"));
        assertEquals(species.size(), new HashSet<>(species).size());
        assertTrue(species.indexOf("Aardvark") < species.indexOf("Koala"));
        assertTrue(species.indexOf("Koala") < species.indexOf("zebra finch"));
    }

    @Test
    void settingFavouriteUpdatesTheObservationFlag() {
        service.setFavourite(observation, owner, true);

        assertTrue(observation.getIsFavourite());
    }

    @Test
    void favouritesAreSavedAndReloadedForTheViewer() {
        service.setFavourite(observation, owner, true);

        assertTrue(markedFavourite(service.getAll(owner), observation.getId()));
        assertTrue(markedFavourite(service.getMine(owner), observation.getId()));
    }

    @Test
    void favouritesBelongToTheViewer() {
        service.setFavourite(observation, owner, true);

        assertFalse(markedFavourite(service.getAll(other), observation.getId()));
    }

    @Test
    void getFavouritesReturnsOnlyTheViewersFavourites() {
        service.setFavourite(observation, owner, true);

        assertEquals(1, service.getFavourites(owner).size());
        assertEquals(observation.getId(), service.getFavourites(owner).get(0).getId());
        assertTrue(service.getFavourites(other).isEmpty());
    }

    @Test
    void removingFavouriteRemovesItFromTheList() {
        service.setFavourite(observation, owner, true);
        service.setFavourite(observation, owner, false);

        assertTrue(service.getFavourites(owner).isEmpty());
    }

    @Test
    void noFavouritesWhenNobodyIsLoggedIn() {
        assertTrue(service.getFavourites(null).isEmpty());
    }

    @Test
    void nobodyLoggedInCannotFavourite() {
        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.setFavourite(observation, null, true));

        assertEquals("Please log in first.", ex.getMessage());
    }

    @Test
    void deletingAnObservationRemovesItFromEveryonesFavourites() {
        service.setFavourite(observation, owner, true);
        service.setFavourite(observation, other, true);

        service.delete(observation, owner);

        assertTrue(service.getFavourites(owner).isEmpty());
        assertTrue(service.getFavourites(other).isEmpty());
    }

    @Test
    void expertCanComment() {
        service.addComment(observation.getId(), expert, "Looks like a koala.");

        List<Comment> comments = service.getComments(observation.getId());

        assertEquals(1, comments.size());
        assertEquals(expert.getId(), comments.get(0).getAuthor().getId());
        assertEquals("Looks like a koala.", comments.get(0).getText());
    }

    @Test
    void ordinaryUserCannotComment() {
        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.addComment(observation.getId(), other, "Nice."));

        assertEquals("Only experts can add comments.", ex.getMessage());
        assertTrue(service.getComments(observation.getId()).isEmpty());
    }

    @Test
    void nobodyLoggedInCannotComment() {
        assertThrows(SecurityException.class,
                () -> service.addComment(observation.getId(), null, "Nice."));
    }

    @Test
    void blankCommentIsRejectedAndNotSaved() {
        assertThrows(InputMismatchException.class,
                () -> service.addComment(observation.getId(), expert, "   "));

        assertTrue(service.getComments(observation.getId()).isEmpty());
    }

    @Test
    void commentsBelongToTheirObservation() {
        Observation second = new Observation(owner, "Perth", "Quokka", "No", DATE);
        observationDAO.addObservation(second);

        service.addComment(observation.getId(), expert, "First.");
        service.addComment(second.getId(), expert, "Second.");

        assertEquals(1, service.getComments(observation.getId()).size());
        assertEquals("First.", service.getComments(observation.getId()).get(0).getText());
        assertEquals("Second.", service.getComments(second.getId()).get(0).getText());
    }
}