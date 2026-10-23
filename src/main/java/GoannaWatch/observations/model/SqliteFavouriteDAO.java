package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
/**
 * Manages personal observation favourites.
 * Each favourite belongs to an individual account.
 */
public class SqliteFavouriteDAO implements IFavouriteDAO{

    @Override
    public boolean isFavourite(Account account, int observationId) {

        String sql = """
                SELECT 1
                FROM account_favourites
                WHERE account_id = ?
                    AND observation_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, account.getId());
            statement.setInt(2, observationId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load favourite.", e);
        }
    }

    @Override
    public void setFavourite(Account account, int observationId, boolean favourite) {

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

            statement.setInt(1, account.getId());
            statement.setInt(2, observationId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save favourite.", e);
        }
    }

    @Override
    public Set<Integer> getFavouriteObservationIds(Account account) {
        String sql = "SELECT observation_id FROM account_favourites WHERE account_id = ?";
        Set<Integer> ids = new HashSet<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, account.getId());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ids.add(result.getInt("observation_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load favourites.", e);
        }
        return ids;
    }

}