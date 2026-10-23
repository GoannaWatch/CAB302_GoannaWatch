package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import GoannaWatch.account.model.SqliteAccountDAO;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and reads observation comments in SQLite.
 */
public class SqliteCommentDAO implements ICommentDAO{

    @Override
    public void addComment(Comment comment) {
        Account account = Session.getCurrentAccount();

        // Only experts can add comments.
        if (account == null || !account.isExpert()) {
            throw new SecurityException("Only experts can add comments.");
        }

        String sql = """
                INSERT INTO comments
                (observation_id, author_id, text)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, comment.getObservationId());
            statement.setInt(2, account.getId());
            statement.setString(3, comment.getText());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add comment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Comment> getCommentsByObservation(int observationId) {
        List<Comment> comments = new ArrayList<>();
        SqliteAccountDAO accountDAO = new SqliteAccountDAO();

        String sql = """
            SELECT comments.*, accounts.email AS author_email
            FROM comments
            JOIN accounts ON comments.author_id = accounts.id
            WHERE comments.observation_id = ?
            ORDER BY comments.id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, observationId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Account author = accountDAO.getAccountByEmail(
                            result.getString("author_email")
                    );

                    Comment comment = new Comment(
                            result.getInt("observation_id"),
                            author,
                            result.getString("text")
                    );

                    comments.add(comment);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get comments: " + e.getMessage(), e);
        }

        return comments;
    }
}