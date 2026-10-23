package GoannaWatch.observations.model;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.account.model.SqliteAccountDAO;
import GoannaWatch.database.DatabaseConnection;
import GoannaWatch.database.DatabaseInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.InputMismatchException;
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

    /**
     * Verifies that a user must be logged in before they can add an observation comment
     */
    @Test
    void testUnauthenticatedUserCannotAddComment() {
        Account oldAccount = Session.getCurrentAccount();

        try {
            Session.clear();

            Account author = new Account(
                    "Test",
                    "User",
                    "test@example.com",
                    "Test123!"
            );

            Comment comment = new Comment(
                    1,
                    author,
                    "This should not be saved."
            );

            SqliteCommentDAO dao = new SqliteCommentDAO();

            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> dao.addComment(comment)
            );

            assertEquals(
                    "Only experts can add comments.",
                    exception.getMessage()
            );

        } finally {
            Session.setCurrentAccount(oldAccount);
        }
    }

    /**
     * Verifies that the DAO attributes comments to the authenticated expert rather than
     * trusting a different author supplied in the comment object.
     */
    @Test
    void testCommentUsesLoggedInExpertAsAuthor(@TempDir Path folder) throws Exception {
        String oldUrl = System.getProperty("goannawatch.db.url");
        Account oldAccount = Session.getCurrentAccount();

        try {
            System.setProperty(
                    "goannawatch.db.url",
                    "jdbc:sqlite:" + folder.resolve("comment-author.db").toAbsolutePath()
            );

            Session.clear();
            DatabaseInitializer.initialize();

            SqliteAccountDAO accountDAO = new SqliteAccountDAO();
            accountDAO.createDefaultExpert();
            Account expert = accountDAO.getAccountByEmail(
                    "expert@123.com"
            );

            assertNotNull(expert);
            assertTrue(expert.isExpert());

            Account normalUser = new Account(
                    "Normal",
                    "User",
                    "normal@example.com",
                    "Test123!"
            );

            accountDAO.addAccount(normalUser);

            Account savedUser = accountDAO.getAccountByEmail(
                    "normal@example.com"
            );

            assertNotNull(savedUser);

            Observation observation = new Observation(
                    savedUser,
                    "Brisbane",
                    "Goanna",
                    "No",
                    LocalDate.of(2026, 10, 20)
            );

            SqliteObservationDAO observationDAO = new SqliteObservationDAO();

            observationDAO.addObservation(observation);

            // Expert is actual logged-in account
            Session.setCurrentAccount(expert);

            // Deliberately supply normal user as author
            Comment comment = new Comment(
                    observation.getId(),
                    savedUser,
                    "Expert identification."
            );

            SqliteCommentDAO commentDAO = new SqliteCommentDAO();

            commentDAO.addComment(comment);

            List<Comment> comments =
                    commentDAO.getCommentsByObservation(
                            observation.getId()
                    );

            assertEquals(1, comments.size());

            // Stored author must be logged-in expert
            assertEquals(
                    expert.getId(),
                    comments.get(0).getAuthor().getId()
            );

            assertNotEquals(
                    savedUser.getId(),
                    comments.get(0).getAuthor().getId()
            );

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
     * Verifies that comments are retrieved only for observation they belong to.
     */
    @Test
    void testCommentsBelongToCorrectObservation(@TempDir Path folder) throws Exception {
        String oldUrl = System.getProperty("goannawatch.db.url");
        Account oldAccount = Session.getCurrentAccount();

        try {
            System.setProperty(
                    "goannawatch.db.url",
                    "jdbc:sqlite:" + folder.resolve("comment-isolation.db").toAbsolutePath()
            );

            Session.clear();
            DatabaseInitializer.initialize();

            SqliteAccountDAO accountDAO = new SqliteAccountDAO();
            accountDAO.createDefaultExpert();

            Account expert = accountDAO.getAccountByEmail(
                    "expert@123.com"
            );

            assertNotNull(expert);
            Session.setCurrentAccount(expert);

            SqliteObservationDAO observationDAO = new SqliteObservationDAO();

            Observation firstObservation = new Observation(
                    expert,
                    "Brisbane",
                    "Goanna",
                    "No",
                    LocalDate.of(2026, 10, 20)
            );

            Observation secondObservation = new Observation (
                    expert,
                    "Gold Coast",
                    "Water Dragon",
                    "No",
                    LocalDate.of(2026, 10, 21)
            );

            observationDAO.addObservation(firstObservation);
            observationDAO.addObservation(secondObservation);

            assertNotEquals(
                    firstObservation.getId(),
                    secondObservation.getId()
            );

            SqliteCommentDAO commentDAO = new SqliteCommentDAO();

            commentDAO.addComment(new Comment(
                    firstObservation.getId(),
                    expert,
                    "First observation comment."
            ));

            commentDAO.addComment(new Comment(
                    secondObservation.getId(),
                    expert,
                    "Second observation comment."
            ));

            List<Comment> firstComments =
                    commentDAO.getCommentsByObservation(
                            firstObservation.getId()
                    );

            List<Comment> secondComments =
                    commentDAO.getCommentsByObservation(
                            secondObservation.getId()
                    );

            assertEquals(1, firstComments.size());
            assertEquals(1, secondComments.size());

            assertEquals(
                    "First observation comment.",
                    firstComments.get(0).getText()
            );

            assertEquals(
                    "Second observation comment.",
                    secondComments.get(0).getText()
            );

            assertEquals(
                    firstObservation.getId(),
                    firstComments.get(0).getObservationId()
            );

            assertEquals(
                    secondObservation.getId(),
                    secondComments.get(0).getObservationId()
            );

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
     * Verifies that comments cannot contain null, empty or whitespace-only text
     */
    @Test
    void testBlankCommentsAreRejected() {
        Account author = new Account(
                "Expert",
                "Test",
                "expert@example.com",
                "Test123!"
        );

        assertThrows(
                InputMismatchException.class,
                () -> new Comment(1, author, null)
        );

        assertThrows(
                InputMismatchException.class,
                () -> new Comment(1, author, "")
        );

        assertThrows(
                InputMismatchException.class,
                () -> new Comment(1, author, " ")
        );

        assertThrows(
                InputMismatchException.class,
                () -> new Comment(1, author, "\t\n")
        );


    }



}