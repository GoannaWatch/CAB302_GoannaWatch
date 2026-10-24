package GoannaWatch.account.model;

import GoannaWatch.database.DatabaseInitializer;
import GoannaWatch.observations.model.SqliteCommentDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.SqliteAccountDAO;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests saving and reading accounts in a temporary database.
 */
public class SqliteAccountDAOTest {

    @org.junit.jupiter.api.AfterEach
    void clearSessionAfterTest() {
        Session.clear();
    }


    /**
     * Checks that a saved account can be found by email.
     *
     * @param folder temporary folder provided by JUnit
     * @throws Exception if database setup or an account operation fails
     */
    @Test
    public void testSaveAccount(@TempDir Path folder) throws Exception {
        String testUrl = "jdbc:sqlite:"
                + folder.resolve("test.db").toAbsolutePath();

        String oldUrl = System.getProperty("goannawatch.db.url");

        // Create the tables in the temporary database.
        try {
            System.setProperty("goannawatch.db.url", testUrl);
            DatabaseInitializer.initialize();

            SqliteAccountDAO dao = new SqliteAccountDAO();

            Account account = new Account(
                    "Min",
                    "Test",
                    "min@example.com",
                    "Test123!"
            );

            dao.addAccount(account);

            // Read the account back to check what was saved.
            Account savedAccount = dao.getAccountByEmail("min@example.com");

            assertNotNull(savedAccount);
            assertEquals("Min Test", savedAccount.getFullName());
            assertEquals("min@example.com", savedAccount.getEmail());
            // Check the correct and incorrect passwords.
            assertTrue(dao.checkPassword("min@example.com", "Test123!"));
            assertFalse(dao.checkPassword("min@example.com", "Wrong123!"));

            // Restore the setting so it does not affect other tests.
        } finally {
            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }
    @Test
    public void testUpdateProfile(@TempDir Path folder) throws Exception {
        String testUrl = "jdbc:sqlite:"
                + folder.resolve("test.db").toAbsolutePath();
        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty("goannawatch.db.url", testUrl);
            DatabaseInitializer.initialize();

            SqliteAccountDAO dao = new SqliteAccountDAO();

            Account account = new Account(
                    "Min", "Test", "min@example.com", "Test123!"
            );
            dao.addAccount(account);

            Account savedAccount = dao.getAccountByEmail("min@example.com");
            assertNotNull(savedAccount);

            savedAccount.setFirstName("Alex");
            savedAccount.setLastName("Wang");
            savedAccount.setEmail("alex@example.com");

            dao.updateAccount(savedAccount);

            Account updatedAccount = dao.getAccountByEmail("alex@example.com");
            assertNotNull(updatedAccount);
            assertEquals("Alex Wang", updatedAccount.getFullName());
            assertEquals(savedAccount.getId(), updatedAccount.getId());
            assertTrue(dao.checkPassword("alex@example.com", "Test123!"));

        } finally {
            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }

    @FunctionalInterface
    private interface DatabaseTestAction {
        void run() throws Exception;
    }

    /**
     * Runs a test using an isolated temp SQLite database
     */
    private void runWithTestDatabase(
            Path folder,
            String filename,
            DatabaseTestAction action
    ) throws Exception {
        String oldUrl = System.getProperty("goannawatch.db.url");

        try {
            System.setProperty(
                    "goannawatch.db.url",
                    "jdbc:sqlite:" + folder.resolve(filename).toAbsolutePath()
            );

            Session.clear();
            DatabaseInitializer.initialize();

            action.run();
        } finally {
            Session.clear();

            if (oldUrl == null) {
                System.clearProperty("goannawatch.db.url");
            } else {
                System.setProperty("goannawatch.db.url", oldUrl);
            }
        }
    }

    /**
     * Verifies that an expert can grant expert access to a normal user
     */
    @Test
    void testExpertCanPromoteUser(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "expert-promotion.db", () -> {
            SqliteAccountDAO dao = new SqliteAccountDAO();
            dao.createDefaultExpert();
            Account expert = dao.getAccountByEmail(
                    "expert@123.com"
            );

            assertNotNull(expert);
            assertTrue(expert.isExpert());

            Account user = new Account(
                    "Normal",
                    "User",
                    "normal@example.com",
                    "Test123!"
            );

            dao.addAccount(user);

            Account savedUser = dao.getAccountByEmail(
                    "normal@example.com"
            );

            assertNotNull(savedUser);
            assertFalse(savedUser.isExpert());

            Session.setCurrentAccount(expert);

            dao.grantExpert(savedUser.getId());

            Account promotedUser = dao.getAccountByEmail(
                    "normal@example.com"
            );

            assertNotNull(promotedUser);
            assertTrue(promotedUser.isExpert());
            assertEquals("expert", promotedUser.getRole());
        });
    }

    /**
     * Verifies that a normal user cannot grant expert permissions to another account.
     */
    @Test
    void testNormalUserCannotPromoteAnotherUser(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "unauthorised-promotion.db", () -> {
            SqliteAccountDAO dao = new SqliteAccountDAO();
            AccountService service = new AccountService(dao);

            dao.addAccount(new Account(
                    "Normal",
                    "User",
                    "normal@example.com",
                    "Test123!"
            ));

            dao.addAccount(new Account(
                    "Target",
                    "User",
                    "target@example.com",
                    "Test123!"
            ));

            Account normalUser = dao.getAccountByEmail(
                    "normal@example.com"
            );

            Account target = dao.getAccountByEmail(
                    "target@example.com"
            );

            assertNotNull(normalUser);
            assertNotNull(target);

            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> service.grantExpert(normalUser, target.getId())
            );

            assertEquals(
                    "Only experts can grant expert access.",
                    exception.getMessage()
            );

            Account unchangedTarget = dao.getAccountByEmail(
                    "target@example.com"
            );

            assertNotNull(unchangedTarget);
            assertFalse(unchangedTarget.isExpert());

        });
    }

    /**
     * Verifies that expert access cannot be granted without an authenticated session.
     */
    @Test
    void testUnauthenticatedUserCannotPromote(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "logged-out-promotion.db",  () -> {
            SqliteAccountDAO dao = new SqliteAccountDAO();
            AccountService service = new AccountService(dao);

            dao.addAccount(new Account(
                    "Target",
                    "User",
                    "target@example.com",
                    "Test123!"
            ));

            Account target = dao.getAccountByEmail(
                    "target@example.com"
            );

            assertNotNull(target);

            assertThrows(
                    SecurityException.class,
                    () -> service.grantExpert(null, target.getId())
            );

            Account unchangedTarget = dao.getAccountByEmail(
                    "target@example.com"
            );

            assertNotNull(unchangedTarget);
            assertFalse(unchangedTarget.isExpert());
        });
    }

    /**
     * Verifies that granting expert access to an account that does not exist is rejected
     */
    @Test
    void testCannotPromoteNonexistentAccount(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "missing-account.db", () -> {
            SqliteAccountDAO dao = new SqliteAccountDAO();
            dao.createDefaultExpert();

            Account expert = dao.getAccountByEmail(
                    "expert@123.com"
            );

            assertNotNull(expert);
            Session.setCurrentAccount(expert);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> dao.grantExpert(999999)
            );

            assertEquals(
                    "Account not found.",
                    exception.getMessage()
            );

        });
    }

    /**
     * Verifies that updating a profile cannot be used by a normal user to give themselves expert access.
     */
    @Test
    void testProfileUpdateCannotPromoteSelf(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "self-role-update.db", () -> {
            SqliteAccountDAO dao = new SqliteAccountDAO();

            dao.addAccount(new Account(
                    "Normal",
                    "User",
                    "normal@example.com",
                    "Test123!"
            ));

            Account user = dao.getAccountByEmail(
                    "normal@example.com"
            );

            assertNotNull(user);
            assertFalse(user.isExpert());

            Session.setCurrentAccount(user);

            user.setRole("expert");
            user.setFirstName("Updated");
            dao.updateAccount(user);

            Account reloaded = dao.getAccountByEmail(
                    "normal@example.com"
            );

            assertNotNull(reloaded);
            assertEquals("Updated", reloaded.getFirstName());
            assertFalse(reloaded.isExpert());
            assertEquals("user", reloaded.getRole());

        });
    }

    /**
     * Verifies that ordinary account updates cannot change another accounts stored permissions
     */
    @Test
    void testProfileUpdateCannotChangeAnotherUsersRole(@TempDir Path folder) throws Exception {
        runWithTestDatabase(folder, "other-role-update.db", () -> {
            SqliteAccountDAO dao = new SqliteAccountDAO();

            dao.addAccount(new Account(
                    "Normal",
                    "User",
                    "normal@example.com",
                    "Test123!"
            ));

            dao.addAccount(new Account(
                    "Target",
                    "User",
                    "target@example.com",
                    "Test123!"
            ));

            Account normalUser = dao.getAccountByEmail(
                    "normal@example.com"
            );

            Account target = dao.getAccountByEmail(
                    "target@example.com"
            );

            assertNotNull(normalUser);
            assertNotNull(target);

            Session.setCurrentAccount(normalUser);

            target.setRole("expert");

            dao.updateAccount(target);

            Account reloadedTarget = dao.getAccountByEmail(
                    "target@example.com"
            );

            assertNotNull(reloadedTarget);
            assertFalse(reloadedTarget.isExpert());
            assertEquals("user", reloadedTarget.getRole());

        });
    }





}