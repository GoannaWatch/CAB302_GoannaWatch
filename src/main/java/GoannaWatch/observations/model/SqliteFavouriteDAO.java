package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manages personal observation favourites.
 * Each favourite belongs to an individual account.
 */
public class SqliteFavouriteDAO {

    /**
     * Returns the ID of the currently logged in account.
     */
    private int currentAccountId() {
        Account current = Session.getCurrentAccount();

        if (current == null) {
            throw new SecurityException("Please log in first.");
        }
        return current.getId();
    }

    /**
     * Checks whether the current user has favourited a particular observation
     */
    public boolean isFavourite(int observationId) {
        int accountId = currentAccountId();

        String sql = """
                SELECT 1
                FROM account_favourites
                WHERE account_id = ?
                    AND observation_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);
            statement.setInt(2, observationId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load favourite.", e);
        }
    }

    /**
     * Adds or removes a favourite for the current user.
     * Does not modify the observation itself
     */
    public void setFavourite(int observationId, boolean favourite) {
        int accountId = currentAccountId();

        String sql = favourite
                ?   """
                    INSERT OR IGNORE INTO account_favourites
                    (account_id, observation_id)
                    VALUES (?, ?)
                    """
                :   """
                    DELETE FROM account_favourites
                    WHERE account_id = ?
                    AND observation_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);
            statement.setInt(2, observationId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save favourite.", e);
        }
    }

}