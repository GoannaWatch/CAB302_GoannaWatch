package GoannaWatch.observations.model;

import GoannaWatch.database.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.SqliteAccountDAO;
import GoannaWatch.database.DatabaseInitializer;

import java.time.LocalDate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Saves and reads wildlife observations in SQLite.
 * Each observation is linked to the account that created it.
 * Query results are returned as Observation objects.
 */
public class SqliteObservationDAO implements IObservationDAO{

    // Saves an observation and assigns its database ID.
    @Override
    public void addObservation(Observation observation) {
        String sql = """
                INSERT INTO observations
                (observer_id, location, animal_seen, is_endangered, observed_at, is_favourite)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, observation.getObserver().getId());
            statement.setString(2, observation.getLocation());
            statement.setString(3, observation.getAnimalSeen());
            statement.setString(4, observation.getIsEndangered());
            statement.setString(5, observation.getObservedAt().toString());
            statement.setBoolean(6, observation.getIsFavourite());

            statement.executeUpdate();

            try (ResultSet result = statement.getGeneratedKeys()) {
                if (result.next()) {
                    observation.setId(result.getInt(1));
                } else {
                    throw new SQLException("No observation ID was returned.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add observation: " + e.getMessage(), e);
        }
    }

    // Finds an observation by ID.
    @Override
    public Observation getObservation(int id) {
        String sql = """
                SELECT observations.*, accounts.email AS observer_email
                FROM observations
                JOIN accounts ON observations.observer_id = accounts.id
                WHERE observations.id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    SqliteAccountDAO accountDAO = new SqliteAccountDAO();
                    Account observer = accountDAO.getAccountByEmail(
                            result.getString("observer_email")
                    );

                    Observation observation = new Observation(
                            observer,
                            result.getString("location"),
                            result.getString("animal_seen"),
                            result.getString("is_endangered"),
                            LocalDate.parse(result.getString("observed_at")),
                            result.getBoolean("is_favourite")
                    );

                    observation.setId(result.getInt("id"));
                    return observation;
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to get observation: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void updateObservation(Observation observation) {
        String sql = """
                UPDATE observations
                SET location = ?, animal_seen = ?, is_endangered = ?, observed_at = ?, is_favourite = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1, observation.getLocation());
            statement.setString(2, observation.getAnimalSeen());
            statement.setString(3, observation.getIsEndangered());
            statement.setString(4, observation.getObservedAt().toString());
            statement.setBoolean(5, observation.getIsFavourite());
            statement.setInt(6, observation.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update observation: " + e.getMessage());
        }
    }

    @Override
    public void deleteObservation(Observation observation) {
        String sql = "DELETE FROM observations WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, observation.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete observation: " + e.getMessage());
        }
    }

    /**
     * Prints saved observations for a manual check.
     *
     * @param args command line arguments, not used
     */
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();

            SqliteObservationDAO dao = new SqliteObservationDAO();
            List<Observation> observations = dao.getAllObservations();


            System.out.println("Total observations: " + observations.size());

            for (Observation observation : observations) {
                System.out.println(
                        observation.getId() + " | "
                                + observation.getObserver().getFullName() + " | "
                                + observation.getLocation() + " | "
                                + observation.getAnimalSeen() + " | "
                                + observation.getIsEndangered() + " | "
                                + observation.getObservedAt() + " | "
                                + observation.getIsFavourite()
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to read observations: " + e.getMessage());
        }
    }

    @Override
    public List<Observation> getAllObservations() {
        List<Observation> observations = new ArrayList<>();

        String sql = """
                SELECT observations.*, accounts.email AS observer_email
                FROM observations
                JOIN accounts ON observations.observer_id = accounts.id
                ORDER BY observations.id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            SqliteAccountDAO accountDAO = new SqliteAccountDAO();

            while (result.next()) {
                Account observer = accountDAO.getAccountByEmail(
                        result.getString("observer_email")
                );

                Observation observation = new Observation(
                        observer,
                        result.getString("location"),
                        result.getString("animal_seen"),
                        result.getString("is_endangered"),
                        LocalDate.parse(result.getString("observed_at")),
                        result.getBoolean("is_favourite")
                );

                observation.setId(result.getInt("id"));
                observations.add(observation);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get observations: " + e.getMessage(), e);
        }

        return observations;
    }

    @Override
    public List<Observation> getObservationsByAccount(Account account) {
        List<Observation> observations = new ArrayList<>();

        String sql = """
                SELECT observations.*, accounts.email AS observer_email
                FROM observations
                JOIN accounts ON observations.observer_id = accounts.id
                WHERE observations.observer_id = ?
                ORDER BY observations.id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, account.getId());

            try (ResultSet result = statement.executeQuery()) {
                SqliteAccountDAO accountDAO = new SqliteAccountDAO();

                while (result.next()) {
                    Account observer = accountDAO.getAccountByEmail(
                            result.getString("observer_email")
                    );

                    Observation observation = new Observation(
                            observer,
                            result.getString("location"),
                            result.getString("animal_seen"),
                            result.getString("is_endangered"),
                            LocalDate.parse(result.getString("observed_at")),
                            result.getBoolean("is_favourite")
                    );

                    observation.setId(result.getInt("id"));
                    observations.add(observation);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get observations by account: " + e.getMessage(), e);
        }

        return observations;
    }

}