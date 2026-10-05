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
                        3,
                        result.getInt("version"),
                        "Database should be migrated to schema version 3"
                );
            }

        } finally {
            restoreDatabaseUrl(previousUrl);
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