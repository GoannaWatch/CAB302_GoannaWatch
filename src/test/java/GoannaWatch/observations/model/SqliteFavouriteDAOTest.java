package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.account.model.SqliteAccountDAO;
import GoannaWatch.database.DatabaseInitializer;
import GoannaWatch.database.DatabaseConnection;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.sql.*;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteFavouriteDAOTest {

    @TempDir
    Path folder;

    private String oldUrl;
    private Account owner;
    private Account otherUser;
    private Observation observation;
    private SqliteFavouriteDAO favouriteDAO;

    /**
     * Creates a fresh database and test accounts before every test
     */
    @BeforeEach
    void setUp() throws Exception {
        oldUrl = System.getProperty("goannawatch.db.url");

        System.setProperty(
                "goannawatch.db.url",
                "jdbc:sqlite:" + folder.resolve("favourites.db").toAbsolutePath()
        );

        Session.clear();
        DatabaseInitializer.initialize();

        SqliteAccountDAO accountDAO = new SqliteAccountDAO();

        accountDAO.addAccount(new Account(
                "Owner",
                "Test",
                "owner@example.com",
                "Test123!"
        ));

        accountDAO.addAccount(new Account(
                "Other",
                "Test",
                "other@example.com",
                "Test123!"
        ));

        owner = accountDAO.getAccountByEmail("owner@example.com");
        otherUser = accountDAO.getAccountByEmail("other@example.com");

        assertNotNull(owner);
        assertNotNull(otherUser);
        assertNotEquals(owner.getId(), otherUser.getId());

        observation = new Observation(
                owner,
                "Brisbane",
                "Goanna",
                "No",
                LocalDate.of(2026, 10, 15)
        );

        SqliteObservationDAO observationDAO = new SqliteObservationDAO();

        observationDAO.addObservation(observation);

        assertTrue(observation.getId() > 0);

        favouriteDAO = new SqliteFavouriteDAO();
    }

    /**
     * Clears the session and restores the original database configuration after every test
     */
    @AfterEach
    void tearDown() {
        if (oldUrl == null) {
            System.clearProperty("goannawatch.db.url");
        } else {
            System.setProperty("goannawatch.db.url", oldUrl);
        }
    }

    /**
     * Newly created observation should not be favourited by the current user
     */
    @Test
    void testNewObservationIsNotFavourite() {
        assertFalse(favouriteDAO.isFavourite(owner, observation.getId()));
    }

    /**
     * Adding an observation to favourites should save the relationship in the database
     */
    @Test
    void testAddFavourite() {
        favouriteDAO.setFavourite(owner, observation.getId(), true);

        assertTrue(favouriteDAO.isFavourite(owner, observation.getId()));
    }

    /**
     * Remove a favourite should update database
     */
    @Test
    void testRemoveFavourite() {
        favouriteDAO.setFavourite(owner, observation.getId(), true);
        assertTrue(favouriteDAO.isFavourite(owner, observation.getId()));

        favouriteDAO.setFavourite(owner, observation.getId(), false);
        assertFalse(favouriteDAO.isFavourite(owner, observation.getId()));
    }

    /**
     * Favourites must belong to individual accounts.
     * Adding a favourite for one user must not automatically favourite it for another user
     */
    @Test
    void testFavouritesAreUserSpecific() {
        favouriteDAO.setFavourite(owner, observation.getId(), true);

        assertTrue(favouriteDAO.isFavourite(owner, observation.getId()));
        assertFalse(favouriteDAO.isFavourite(otherUser, observation.getId()));

        favouriteDAO.setFavourite(otherUser, observation.getId(), true);
        assertTrue(favouriteDAO.isFavourite(otherUser, observation.getId()));

        // Removing the owner's favourite must not remove the other user's.
        favouriteDAO.setFavourite(owner, observation.getId(), false);

        assertFalse(favouriteDAO.isFavourite(owner, observation.getId()));
        assertTrue(favouriteDAO.isFavourite(otherUser, observation.getId()));
    }

    /**
     * Favourites should persist after a user logs out and logs back into application
     */
    @Test
    void testFavouritePersistsAcrossSession() {
        favouriteDAO.setFavourite(owner, observation.getId(), true);

        assertTrue(new SqliteFavouriteDAO().isFavourite(owner, observation.getId()));

    }

    /**
     * Adding the same observation to favourites twice should not create duplicate database records.
     */
    @Test
    void testAddingSameFavouriteTwiceDoesNotDuplicate() throws SQLException {
        favouriteDAO.setFavourite(owner, observation.getId(), true);
        favouriteDAO.setFavourite(owner, observation.getId(), true);

        assertTrue(
                favouriteDAO.isFavourite(owner, observation.getId())
        );

        String sql = """
                SELECT COUNT(*)
                FROM account_favourites
                WHERE account_id = ?
                AND observation_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, owner.getId());
            statement.setInt(2, observation.getId());

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(
                        1,
                        result.getInt(1),
                        "The same favourite should only be stored once."
                );
            }
        }
    }

    /**
     * Users must be logged in before accessing or modifying their favourites
     */

    /**
     * Deleting an observation must remove all favourites associated with that observation
     */
    @Test
    void testDeletingObservationRemovesAssociatedFavourites() throws SQLException {
        favouriteDAO.setFavourite(owner, observation.getId(), true);
        favouriteDAO.setFavourite(otherUser, observation.getId(), true);

        assertTrue(
                favouriteDAO.isFavourite(otherUser, observation.getId())
        );

        SqliteObservationDAO observationDAO = new SqliteObservationDAO();

        observationDAO.deleteObservation(observation);

        // Verify the observation no longer exists
        assertNull(
                observationDAO.getObservation(observation.getId())
        );

        String sql = """
                SELECT COUNT(*)
                FROM account_favourites
                WHERE observation_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, observation.getId());

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());

                assertEquals(
                        0,
                        result.getInt(1),
                        "Deleting an observation should remove all associated favourites."
                );
            }
        }
    }
}