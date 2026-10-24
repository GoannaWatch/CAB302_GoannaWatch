package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.account.model.SqliteAccountDAO;
import GoannaWatch.database.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import GoannaWatch.observations.model.Observation;
import GoannaWatch.observations.model.SqliteObservationDAO;
/**
 * Tests saving and reading observations in a temporary database.
 */
public class SqliteObservationDAOTest {

    @AfterEach
    void clearSessionAfterTest() {
        Session.clear();
    }

    /**
     * Checks that a saved observation keeps its details and account link.
     *
     * @param folder temporary folder provided by JUnit
     * @throws Exception if database setup or a DAO operation fails
     */
    @Test
    public void testSaveObservation(@TempDir Path folder) throws Exception {
        String testUrl = "jdbc:sqlite:"
                + folder.resolve("test.db").toAbsolutePath();

        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty("goannawatch.db.url", testUrl);
            DatabaseInitializer.initialize();

            SqliteAccountDAO accountDAO = new SqliteAccountDAO();
            Account account = new Account(
                    "Min",
                    "Test",
                    "min@example.com",
                    "Test123!"
            );

            // The observation needs an account that exists in the database.
            accountDAO.addAccount(account);
            // Read the account back to get its database ID.
            Account savedAccount = accountDAO.getAccountByEmail(
                    "min@example.com"
            );
            assertNotNull(savedAccount);

            LocalDate date = LocalDate.of(2026, 9, 15);
            Observation observation = new Observation(
                    savedAccount,
                    "Brisbane",
                    "Goanna",
                    "No",
                    date
            );

            SqliteObservationDAO dao = new SqliteObservationDAO();
            dao.addObservation(observation);

            // Use the ID assigned when the observation was saved.
            Observation savedObservation = dao.getObservation(
                    observation.getId()
            );

            assertNotNull(savedObservation);
            assertEquals("Brisbane", savedObservation.getLocation());
            assertEquals("Goanna", savedObservation.getAnimalSeen());
            assertEquals(date, savedObservation.getObservedAt());
            assertEquals(
                    savedAccount.getId(),
                    savedObservation.getObserver().getId()
            );

        } finally {
            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }

    /**
     * Creates and saves a test account, then retrieves
     * the database-generated account ID.
     */
    private Account createTestAccount(String firstName, String email) {
        SqliteAccountDAO accountDAO = new SqliteAccountDAO();

        Account account = new Account(
                firstName,
                "Test",
                email,
                "Test123!"
        );

        accountDAO.addAccount(account);

        Account savedAccount = accountDAO.getAccountByEmail(email);

        assertNotNull(savedAccount);

        return savedAccount;

    }

    /**
     * Creates an observation owned by the supplied account.
     */
    private Observation createTestObservation(Account owner) {

        Observation observation = new Observation(
                owner,
                "Brisbane",
                "Goanna",
                "No",
                LocalDate.of(2026, 9, 15)
        );

        SqliteObservationDAO dao = new SqliteObservationDAO();

        dao.addObservation(observation);

        assertTrue(observation.getId() > 0);

        return observation;
    }

    /**
     * Verifies that the observation owner can edit their own observation
     */
    @Test
    void testOwnerCanEditObservation(@TempDir Path folder) throws Exception {
        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty(
                    "goannawatch.db.url",
                    "jdbc:sqlite:" + folder.resolve("owner-edit.db").toAbsolutePath()
            );

            DatabaseInitializer.initialize();

            Account owner = createTestAccount(
                    "Owner",
                    "owner@example.com"
            );

            Observation observation = createTestObservation(owner);

            Observation updatedObservation = new Observation(
                    owner,
                    "Gold Coast",
                    "Water Dragon",
                    "No",
                    LocalDate.of(2026, 10, 1)
            );

            updatedObservation.setId(observation.getId());

            SqliteObservationDAO dao = new SqliteObservationDAO();

            assertDoesNotThrow(() ->
                    dao.updateObservation(updatedObservation));

            Observation saved = dao.getObservation(observation.getId());

            assertNotNull(saved);
            assertEquals("Gold Coast", saved.getLocation());
            assertEquals("Water Dragon", saved.getAnimalSeen());
            assertEquals(LocalDate.of(2026, 10, 1), saved.getObservedAt());
            assertEquals(owner.getId(), saved.getObserver().getId());

        } finally {

            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }

    }

    /**
     * Verifies that an expert can edit an observation belonging to another user.
     */
    @Test
    void testExpertCanEditAnotherUsersObservation(@TempDir Path folder) throws Exception {

        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty(
                    "goannawatch.db.url",
                    "jdbc:sqlite:" + folder.resolve("expert-edit.db").toAbsolutePath()
            );

            DatabaseInitializer.initialize();

            Account owner = createTestAccount(
                    "Owner",
                    "owner@example.com"
            );

            Account expert = createTestAccount(
                    "Expert",
                    "expert-edit@example.com"
            );

            expert.setRole("expert");

            Observation observation = createTestObservation(owner);

            Observation updatedObservation = new Observation(
                    owner,
                    "Sunshine Coast",
                    "Eastern Bearded Dragon",
                    "No",
                    LocalDate.of(2026, 10, 3)
            );

            updatedObservation.setId(observation.getId());

            SqliteObservationDAO dao = new SqliteObservationDAO();

            assertDoesNotThrow(() ->
                    dao.updateObservation(updatedObservation));

            Observation saved = dao.getObservation(observation.getId());

            assertNotNull(saved);
            assertEquals("Sunshine Coast", saved.getLocation());
            assertEquals("Eastern Bearded Dragon", saved.getAnimalSeen());
            assertEquals(owner.getId(), saved.getObserver().getId());

        } finally {

            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }

        }
    }

    @FunctionalInterface
    private interface DatabaseTestAction {
        void run() throws Exception;
    }

    private void runWithTestDatabase(
            Path folder,
            String filename,
            DatabaseTestAction action
    ) throws Exception {
        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty(
                    "goannawatch.db.url",
                    "jdbc:sqlite:" + folder.resolve(filename).toAbsolutePath()
            );

            DatabaseInitializer.initialize();

            action.run();
        } finally {

            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }

    /**
     * Verifies that an observation owner can delete their own observation
     */
    @Test
    void testOwnerCanDeleteObservation(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "owner-delete.db", () -> {
            Account owner = createTestAccount(
                    "Owner", "owner@example.com"
            );

            Observation observation = createTestObservation(owner);

            SqliteObservationDAO dao = new SqliteObservationDAO();

            assertDoesNotThrow(() ->
                    dao.deleteObservation(observation));

            assertNull(dao.getObservation(observation.getId()));

        });
    }

    /**
     * Verifies that an expert can delete an observation belonging to another user.
     */
    @Test
    void testExpertCanDeleteAnotherUsersObservation(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "expert-delete.db", () -> {
            Account owner = createTestAccount(
                    "Owner", "owner@example.com"
            );

            Account expert = createTestAccount(
                    "Expert", "expert@example.com"
            );

            expert.setRole("expert");

            Observation observation = createTestObservation(owner);

            SqliteObservationDAO dao = new SqliteObservationDAO();

            assertDoesNotThrow(() ->
                    dao.deleteObservation(observation));

            assertNull(dao.getObservation(observation.getId()));

        });
    }

    /**
     * Verifies that editing or deleting a nonexistent observation is rejected.
     */
    @Test
    void testNonexistentObservationCannotBeModified(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "nonexistent-observation.db", () -> {

            Account owner = createTestAccount(
                    "Owner", "owner@example.com"
            );

            Observation observation = new Observation(
                    owner,
                    "Brisbane",
                    "Goanna",
                    "No",
                    LocalDate.of(2026, 10, 1)
            );

            observation.setId(999999);

            SqliteObservationDAO dao = new SqliteObservationDAO();

            assertDoesNotThrow(() -> dao.updateObservation(observation));
            assertDoesNotThrow(() -> dao.deleteObservation(observation));
            assertNull(dao.getObservation(999999));

        });
    }

    /**
     * Verifies that a place's coordinates are saved and loaded.
     */
    @Test
    void testCoordinatesAreSavedAndLoaded(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "coordinates.db", () -> {
            Account owner = createTestAccount("Owner", "owner@example.com");
            Observation observation = new Observation(owner, "Lone Pine", -27.53, 152.97,
                    "Koala", "Yes", LocalDate.of(2026, 10, 5), false);

            SqliteObservationDAO dao = new SqliteObservationDAO();
            dao.addObservation(observation);

            Observation saved = dao.getObservation(observation.getId());
            assertTrue(saved.hasCoordinates());
            assertEquals(-27.53, saved.getLatitude(), 0.0001);
            assertEquals(152.97, saved.getLongitude(), 0.0001);
        });
    }

    /**
     * Verifies that a missing location stays null
     * */
    @Test
    void testObservationWithoutCoordinatesLoadsWithNulls(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "no-coordinates.db", () -> {
            Account owner = createTestAccount("Owner", "owner@example.com");
            Observation observation = createTestObservation(owner);

            Observation saved = new SqliteObservationDAO().getObservation(observation.getId());

            assertFalse(saved.hasCoordinates());
            assertNull(saved.getLatitude());
            assertNull(saved.getLongitude());
        });
    }

    /**
     * Verifies that choosing a place for an existing observation is saved.
     */
    @Test
    void testUpdatingPlaceSavesCoordinates(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "update-place.db", () -> {
            Account owner = createTestAccount("Owner", "owner@example.com");
            Observation observation = createTestObservation(owner);

            observation.setPlace(new Place("Gold Coast", -28.0, 153.4));
            SqliteObservationDAO dao = new SqliteObservationDAO();
            dao.updateObservation(observation);

            Observation saved = dao.getObservation(observation.getId());
            assertEquals("Gold Coast", saved.getLocation());
            assertEquals(-28.0, saved.getLatitude(), 0.0001);
            assertEquals(153.4, saved.getLongitude(), 0.0001);
        });
    }

    /**
     * Verifies that observations can be filtered to one account.
     */
    @Test
    void testGetObservationsByAccountOnlyReturnsThatAccount(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "by-account.db", () -> {
            Account first = createTestAccount("First", "first@example.com");
            Account second = createTestAccount("Second", "second@example.com");
            Observation firstObservation = createTestObservation(first);
            createTestObservation(second);

            List<Observation> result = new SqliteObservationDAO().getObservationsByAccount(first);

            assertEquals(1, result.size());
            assertEquals(firstObservation.getId(), result.get(0).getId());
            assertEquals(first.getId(), result.get(0).getObserver().getId());
        });
    }

    /**
     * Verifies that every observation is returned, oldest ID first.
     */
    @Test
    void testGetAllObservationsReturnsEveryRowInIdOrder(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "all-observations.db", () -> {
            Account owner = createTestAccount("Owner", "owner@example.com");
            Observation first = createTestObservation(owner);
            Observation second = createTestObservation(owner);

            List<Observation> all = new SqliteObservationDAO().getAllObservations();

            assertEquals(2, all.size());
            assertEquals(first.getId(), all.get(0).getId());
            assertEquals(second.getId(), all.get(1).getId());
        });
    }



}