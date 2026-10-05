package GoannaWatch.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates and updates the GoannaWatch database.
 * Database changes are applied as numbered migrations. Each migration only runs once for a particular database.
 */
public class DatabaseInitializer {

    private static final int CURRENT_DATABASE_VERSION = 3;

    /**
     * Creates the database tables and applies any required migrations.
     * @throws SQLException if database initialization or migration fails.
     */
    public static void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Do all schema changes together so a failed migration does not leave database partially updated
            connection.setAutoCommit(false);

            try {
                createSchemaVersionTable(connection);

                int version = getDatabaseVersion(connection);

                /*
                Version 1:
                Initial accounts and observations tables.
                 */
                if (version < 1) {
                    migrateToVersion1(connection);
                    setDatabaseVersion(connection, 1);
                    version = 1;
                }

                /*
                Version 2:
                Add endangered-status information to observations.
                 */
                if (version < 2) {
                    migrateToVersion2(connection);
                    setDatabaseVersion(connection, 2);
                    version = 2;
                }

                /*
                Version 3:
                Add favourite status to observations.
                */
                if (version < 3) {
                    migrateToVersion3(connection);
                    setDatabaseVersion(connection, 3);
                    version = 3;
                }

                /*
                Confirm that all required migrations were completed
                 */
                if (version != CURRENT_DATABASE_VERSION) {
                    throw new SQLException(
                            "Database schema version mismatch. Expected "
                                + CURRENT_DATABASE_VERSION
                                + " but found "
                                + version
                    );
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        }
    }

    /**
     * Stores the current database schema version.
     */
    private static void createSchemaVersionTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER NOT NULL
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);

            /*
            Brand new schema_version table contains no row.
            Insert version 0 so migrations know that nothing has been applied yet
             */
            try (ResultSet result = statement.executeQuery(
                    "SELECT COUNT(*) AS count FROM schema_version")) {
                if (result.next() && result.getInt("count") == 0) {
                    statement.executeUpdate(
                            "INSERT INTO schema_version (version) VALUES (0)"
                    );
                }
            }
        }
    }

    /**
     * Returns the schema version currently stored in the database.
     */
    private static int getDatabaseVersion(Connection connection) throws SQLException {
        String sql = "SELECT version FROM schema_version LIMIT 1";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (result.next()) {
                return result.getInt("version");
            }

            return 0;
        }
    }

    /**
     * Updates the schema version after a successful migration.
     */
    private static void setDatabaseVersion(
            Connection connection,
            int version) throws SQLException {
        String sql = "UPDATE schema_version SET version = " + version;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * Version 1 of the database.
     * Creates the original accoutns and obserrvation tables.
     */
    private static void migrateToVersion1(Connection connection) throws SQLException {

        String accountSql = """
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY,
                        first_name TEXT NOT NULL,
                        last_name TEXT NOT NULL,
                        email TEXT NOT NULL COLLATE NOCASE UNIQUE,
                        password TEXT NOT NULL
                    )
                    """;

        String observationSql = """
            CREATE TABLE IF NOT EXISTS observations (
                id INTEGER PRIMARY KEY,
                observer_id INTEGER NOT NULL,
                location TEXT NOT NULL,
                animal_seen TEXT NOT NULL,
                observed_at TEXT NOT NULL,
                FOREIGN KEY (observer_id) REFERENCES accounts(id)
            )
            """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(accountSql);
            statement.executeUpdate(observationSql);
        }
    }

    /**
     * Version 2 of the database.
     * Adds is_endangered to observations if the column is not already present.
     */
    private static void migrateToVersion2(Connection connection) throws SQLException {
        if (!columnExists(connection, "observations", "is_endangered")) {
            String sql = """
                    ALTER TABLE observations
                    ADD COLUMN is_endangered TEXT NOT NULL DEFAULT 'No'
                    """;

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        }
    }

    private static void migrateToVersion3(Connection connection) throws SQLException {
        if (!columnExists(connection, "observations", "is_favourite")) {
            String sql = """
                ALTER TABLE observations
                ADD COLUMN is_favourite INTEGER NOT NULL DEFAULT 0
                """;

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        }
    }


    /**
     * Checks whether a table contains a particular column.
     */
    private static boolean columnExists(
            Connection connection,
            String tableName,
            String columnName) throws SQLException {

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                if (columnName.equals(result.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Runs a small manual database initialization check.
     */
    public static void main(String[] args) {
        try {
            initialize();

            try (Connection connection = DatabaseConnection.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "SELECT version FROM schema_version"
                 )) {

                if (result.next()) {
                    System.out.println(
                            "Database schema version: " + result.getInt("version")
                    );
                }

            }

            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}


