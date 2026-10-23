package GoannaWatch.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests database connections using a temporary SQLite file.
 */
public class DatabaseConnectionTest {

    /**
     * Checks that the connection uses the requested database file.
     *
     * @param folder temporary folder provided by JUnit
     * @throws Exception if the database connection fails
     */

    @Test
    public void connectsToTemporaryDatabase(@TempDir Path folder) throws Exception {

        Path databaseFile = folder.resolve("test.db");
        String testUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();


        String previousUrl = System.getProperty("goannawatch.db.url");

        // Use a temporary database instead of the app's database.
        try {
            System.setProperty("goannawatch.db.url", testUrl);

            try (Connection connection = DatabaseConnection.getConnection()) {
                assertEquals(testUrl, connection.getMetaData().getURL());
                assertTrue(Files.exists(databaseFile));
            }
        } finally {
            // Restore the setting so it does not affect other tests.
            restoreDatabaseUrl(previousUrl);
        }
    }

    /**
     * Checks that initializing a new database creates the current observation schema.
     * @param folder temporary folder provided by JUnit
     * @throws Exception if initialization fails
     */
    @Test
    public void initializerCreatesCurrentObservationSchema(
            @TempDir Path folder) throws Exception {

        Path databaseFile = folder.resolve("current-schema.db");

        String testUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();

        String previousUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty("goannawatch.db.url", testUrl);

            DatabaseInitializer.initialize();

            try (Connection connection =
                         DatabaseConnection.getConnection();
                 Statement statement =
                         connection.createStatement();
                 ResultSet result =
                         statement.executeQuery(
                                 "PRAGMA table_info(observations)"
                         )) {

                boolean endangeredColumnFound = false;

                while (result.next()) {
                    if ("is_endangered".equals(
                            result.getString("name"))) {
                        endangeredColumnFound = true;
                        break;
                    }
                }

                assertTrue(
                        endangeredColumnFound,
                        "The observations table should contain " + "the is_endangered column"
                );
            }
        } finally {
            restoreDatabaseUrl(previousUrl);
        }

    }

    /**
     * Recreates old observations schema that did not contain is_endangered and checks that
     * DatabaseInitializer updates it.
     *
     * This protects against the database error experienced when older local databases are opened with
     * newer application code.
     *
     * @param folder temporary folder provided by JUnit
     * @throws Exception if migration fails
     */
    @Test
    public void initializerUpgradesOldObservationSchema(
            @TempDir Path folder) throws Exception {

        Path databaseFile =
                folder.resolve("old-schema.db");

        String testUrl =
                "jdbc:sqlite:" + databaseFile.toAbsolutePath();

        String previousUrl =
                System.getProperty("goannawatch.db.url");

        try {
            System.setProperty("goannawatch.db.url", testUrl);

            /*
            Recreate the previous database structure from before the is_endangered column
             */
            try (Connection connection =
                    DatabaseConnection.getConnection();
                Statement statement =
                    connection.createStatement()) {

                statement.executeUpdate("""
                    CREATE TABLE accounts (
                        id INTEGER PRIMARY KEY,
                        first_name TEXT NOT NULL,
                        last_name TEXT NOT NULL,
                        email TEXT NOT NULL COLLATE NOCASE UNIQUE,
                        password TEXT NOT NULL
                    )
                    """);

                statement.executeUpdate("""
                    CREATE TABLE observations (
                        id INTEGER PRIMARY KEY,
                        observer_id INTERGER NOT NULL,
                        location TEXT NOT NULL,
                        animal_seen TEXT NOT NULL,
                        observed_at TEXT NOT NULL,
                        FOREIGN KEY (observer_id)
                            REFERENCES accounts(id)
                    )
                    """);

                /*
                Add an account before migration to check that its data is preserved.
                The password value is only a test placeholder, not used for login.
                */
                statement.executeUpdate("""
                        INSERT INTO accounts
                        (id, first_name, last_name, email, password)
                        VALUES (1, 'Min', 'Test', 'min@example.com', 'test-placeholder')
                        """);

            }

            // Run the current database initialization/migration code.
            DatabaseInitializer.initialize();

            try(Connection connection =
                        DatabaseConnection.getConnection();
                Statement statement =
                        connection.createStatement();
                ResultSet result =
                        statement.executeQuery(
                                "PRAGMA table_info(observations)"
                        )) {

                boolean endangeredColumnFound = false;

                while (result.next()) {
                    if ("is_endangered".equals(
                            result.getString("name"))) {
                        endangeredColumnFound = true;
                        break;
                    }
                }

                assertTrue(
                        endangeredColumnFound,
                        "An old observations table should be updated " + "to include is_endangered"
                );
            }

        } finally {
            restoreDatabaseUrl(previousUrl);
        }

    }

    /**
     * Checks that database initialization records the current schema version after all migrations
     *
     * @param folder tempory folder provided by JUnit
     * @throws Exception if initialization fails
     */
    @Test
    public void initializerRecordsCurrentSchemaVersion(
            @TempDir Path folder) throws Exception {

        Path databaseFile =
                folder.resolve("schema-version.db");

        String testUrl =
                "jdbc:sqlite:" + databaseFile.toAbsolutePath();

        String previousUrl =
                System.getProperty("goannawatch.db.url");

        try {
            System.setProperty("goannawatch.db.url", testUrl);

            DatabaseInitializer.initialize();

            try (Connection connection =
                    DatabaseConnection.getConnection();
                Statement statement =
                    connection.createStatement();
                ResultSet result =
                    statement.executeQuery(
                            "SELECT version FROM schema_version"
                    )) {

                assertTrue(
                        result.next(),
                        "schema_version should contain a version"
                );

                assertEquals(
                        7,
                        result.getInt("version"),
                        "Database should be migrated to schema version 7"
                );
            }

        } finally {
            restoreDatabaseUrl(previousUrl);
        }

    }

    /**
     * Verifies that running database initialization multiple times preserves existing data.
     */
    @Test
    void testReinitializationPreservesExistingData(@TempDir Path folder) throws Exception {
        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty(
                    "goannawatch.db.url",
                    "jdbc:sqlite:" + folder.resolve("reinitization.db").toAbsolutePath()
            );

            DatabaseInitializer.initialize();

            try (Connection connection = DatabaseConnection.getConnection();
                Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO accounts
                        (id, first_name, last_name, email, password, role)
                        VALUES 
                            (100, 'Test', 'User', 'preserve@example.com', 'placeholder', 'user')
                        """);

                statement.executeUpdate("""
                        INSERT INTO observations
                        (id, observer_id, location, animal_seen, observed_at, is_endangered)
                        VALUES 
                            (200, 100, 'Brisbane', 'Goanna', '2026-10-20', 'No')
                        """);

                statement.executeUpdate("""
                        INSERT INTO account_favourites
                        (account_id, observation_id)
                        VALUES (100, 200)
                        """);
            }

            DatabaseInitializer.initialize();

            try (Connection connection = DatabaseConnection.getConnection();
                Statement statement = connection.createStatement()) {

                try (ResultSet account = statement.executeQuery("""
                        SELECT id
                        FROM accounts
                        WHERE id = 100
                        """)) {

                    assertTrue(
                            account.next(), "Account must survive reinitialization"
                    );
                }

                try (ResultSet observation = statement.executeQuery("""
                        SELECT id
                        FROM observations
                        WHERE id = 200
                        """)) {

                    assertTrue(
                            observation.next(), "Observation must survive reinitialization"
                    );
                }

                try (ResultSet favourite = statement.executeQuery("""
                        SELECT account_id, observation_id
                        FROM account_favourites
                        WHERE account_id = 100
                        AND observation_id = 200
                        """)) {

                    assertTrue(
                            favourite.next(), "Favourite must survive reinitialization"
                    );
                }

            }

        } finally {
            restoreDatabaseUrl(oldUrl);
        }


    }




    /**
     * Restores the database URL system property after a test.
     * @param previousUrl database URL that existed before the test
     */
    private void restoreDatabaseUrl(String previousUrl) {
        if (previousUrl == null) {
            System.clearProperty("goannawatch.db.url");
        } else {
            System.setProperty(
                    "goannawatch.db.url",
                    previousUrl
            );
        }
    }
}