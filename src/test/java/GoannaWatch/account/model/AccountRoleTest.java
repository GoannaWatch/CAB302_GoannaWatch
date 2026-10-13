package GoannaWatch.account.model;

import GoannaWatch.database.DatabaseConnection;
import GoannaWatch.database.DatabaseInitializer;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the ordinary and expert account roles.
 */
public class AccountRoleTest {

    @Test
    public void newAccountIsOrdinaryUser() {
        Account account = new Account("Min", "Test", "min@example.com");

        assertEquals("user", account.getRole());
        assertFalse(account.isExpert());
    }

    @Test
    public void expertAccountIsRecognised() {
        Account account = new Account("Min", "Test", "min@example.com");

        account.setRole("expert");

        assertEquals("expert", account.getRole());
        assertTrue(account.isExpert());
    }

    /**
     * Checks that new accounts use the user role and that account queries
     * return the expert role after it is set in the database.
     *
     * @param folder temporary folder provided by JUnit
     * @throws Exception if database setup or an account query fails
     */
    @Test
    public void testReadExpertRole(@TempDir Path folder) throws Exception {
        String testUrl = "jdbc:sqlite:" + folder.resolve("test.db").toAbsolutePath();
        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty("goannawatch.db.url", testUrl);
            DatabaseInitializer.initialize();

            SqliteAccountDAO dao = new SqliteAccountDAO();
            dao.addAccount(new Account("Min", "Test", "min@example.com", "Test123!"));

            Account ordinaryAccount = dao.getAccountByEmail("min@example.com");
            assertNotNull(ordinaryAccount);
            assertFalse(ordinaryAccount.isExpert());

            // Changes the test account to an expert.
            try (Connection connection = DatabaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        UPDATE accounts
                        SET role = 'expert'
                        WHERE email = 'min@example.com'
                        """);
            }

            Account expertAccount = dao.getAccountByEmail("min@example.com");
            assertNotNull(expertAccount);
            assertTrue(expertAccount.isExpert());

            assertEquals(1, dao.getAllAccounts().size());
            assertTrue(dao.getAllAccounts().get(0).isExpert());

        } finally {
            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }

    /**
     * Checks that experts can grant expert access and ordinary users cannot.
     *
     * @param folder temporary folder provided by JUnit
     * @throws Exception if database setup or account operations fail
     */
    @Test
    public void expertCanGrantAccess(@TempDir Path folder) throws Exception {
        String testUrl = "jdbc:sqlite:"
                + folder.resolve("test.db").toAbsolutePath();

        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty("goannawatch.db.url", testUrl);
            DatabaseInitializer.initialize();

            SqliteAccountDAO dao = new SqliteAccountDAO();

            dao.addAccount(new Account(
                    "Min", "Expert", "expert@example.com", "Test123!"
            ));

            dao.addAccount(new Account(
                    "Test", "User", "user@example.com", "Test123!"
            ));

            Account expert = dao.getAccountByEmail("expert@example.com");
            Account user = dao.getAccountByEmail("user@example.com");

            assertNotNull(expert);
            assertNotNull(user);

            expert.setRole("expert");
            Session.setCurrentAccount(expert);

            dao.grantExpert(user.getId());

            Account updatedUser = dao.getAccountByEmail("user@example.com");
            assertNotNull(updatedUser);
            assertTrue(updatedUser.isExpert());

            Session.setCurrentAccount(user);

            assertThrows(
                    SecurityException.class,
                    () -> dao.grantExpert(expert.getId())
            );

        } finally {
            Session.clear();

            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }
}
