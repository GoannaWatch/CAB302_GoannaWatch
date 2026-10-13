package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.account.model.SqliteAccountDAO;
import GoannaWatch.database.DatabaseInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests saving and reading observation comments.
 */
public class SqliteCommentDAOTest {

    /**
     * Checks that an expert's comment keeps its details after saving.
     *
     * @param folder Temporary folder provided by JUnit.
     * @throws Exception If database setup or a DAO operation fails.
     */
    @Test
    public void testSaveComment(@TempDir Path folder) throws Exception {
        String testUrl = "jdbc:sqlite:"
                + folder.resolve("comments.db").toAbsolutePath();

        String oldUrl = System.getProperty("goannawatch.db.url");
        Account oldAccount = Session.getCurrentAccount();

        try {
            System.setProperty("goannawatch.db.url", testUrl);
            DatabaseInitializer.initialize();

            // Create an expert account in the temporary database.
            SqliteAccountDAO accountDAO = new SqliteAccountDAO();
            accountDAO.createDefaultExpert();

            Account expert = accountDAO.getAccountByEmail("expert@123.com");
            Session.setCurrentAccount(expert);

            // The comment needs an observation saved in the database.
            Observation observation = new Observation(
                    expert,
                    "Brisbane",
                    "Goanna",
                    "No",
                    LocalDate.of(2026, 10, 11)
            );

            SqliteObservationDAO observationDAO = new SqliteObservationDAO();
            observationDAO.addObservation(observation);


            Comment comment = new Comment(
                    observation.getId(),
                    expert,
                    "This looks like a lace monitor."
            );

            SqliteCommentDAO commentDAO = new SqliteCommentDAO();
            commentDAO.addComment(comment);

            // Read the saved comment back from the database.
            List<Comment> comments = commentDAO.getCommentsByObservation(
                    observation.getId()
            );

            assertEquals(1, comments.size());

            Comment savedComment = comments.get(0);
            assertEquals(observation.getId(), savedComment.getObservationId());
            assertEquals(expert.getId(), savedComment.getAuthor().getId());
            assertEquals(comment.getText(), savedComment.getText());

        } finally {
            Session.setCurrentAccount(oldAccount);

            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }
    /**
     * Checks that a normal user cannot add comments.
     */
    @Test
    public void testUserCannotAddComment() {
        Account oldAccount = Session.getCurrentAccount();

        try {
            Account user = new Account(
                    "Min",
                    "User",
                    "user@example.com",
                    "User@123"
            );
            Session.setCurrentAccount(user);

            Comment comment = new Comment(
                    1,
                    user,
                    "Test comment."
            );

            SqliteCommentDAO commentDAO = new SqliteCommentDAO();

            assertThrows(
                    SecurityException.class,
                    () -> commentDAO.addComment(comment)
            );

        } finally {
            Session.setCurrentAccount(oldAccount);
        }
    }
}