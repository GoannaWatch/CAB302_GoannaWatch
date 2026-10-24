package GoannaWatch.account.model;

import GoannaWatch.database.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests login and role rules.
 */
class AccountServiceTest {

    @TempDir
    Path folder;

    private String oldUrl;
    private SqliteAccountDAO dao;
    private AccountService service;

    @BeforeEach
    void setUp() throws Exception {
        oldUrl = System.getProperty("goannawatch.db.url");
        System.setProperty("goannawatch.db.url",
                "jdbc:sqlite:" + folder.resolve("accounts.db").toAbsolutePath());
        DatabaseInitializer.initialize();

        dao = new SqliteAccountDAO();
        service = new AccountService(dao);
    }

    @AfterEach
    void tearDown() {
        if (oldUrl == null) {
            System.clearProperty("goannawatch.db.url");
        } else {
            System.setProperty("goannawatch.db.url", oldUrl);
        }
    }

    private Account addAccount(String email) {
        dao.addAccount(new Account("Luke", "Smith", email, "Password1!"));
        return dao.getAccountByEmail(email);
    }

    @Test
    void correctPasswordReturnsTheAccount() {
        Account account = addAccount("login1@example.com");

        Account result = service.authenticate("login1@example.com", "Password1!");

        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
    }

    @Test
    void emailIsMatchedIgnoringCase() {
        addAccount("login-case@example.com");

        assertNotNull(service.authenticate("LOGIN-CASE@EXAMPLE.COM", "Password1!"));
    }

    @Test
    void wrongPasswordReturnsNull() {
        addAccount("login2@example.com");

        assertNull(service.authenticate("login2@example.com", "Wrong1!pass"));
    }

    @Test
    void unknownEmailReturnsNull() {
        assertNull(service.authenticate("nobody@example.com", "Password1!"));
    }

    @Test
    void getAllAccountsReturnsStoredAccounts() {
        addAccount("all1@example.com");
        addAccount("all2@example.com");

        assertEquals(2, service.getAllAccounts().size());
    }

    @Test
    void expertCanGrantExpertAccess() {
        Account expert = addAccount("expert@example.com");
        dao.grantExpert(expert.getId());
        expert = dao.getAccountByEmail("expert@example.com");
        Account target = addAccount("target1@example.com");

        service.grantExpert(expert, target.getId());

        assertTrue(dao.getAccountByEmail("target1@example.com").isExpert());
    }

    @Test
    void ordinaryUserCannotGrantExpertAccess() {
        Account user = addAccount("user@example.com");
        Account target = addAccount("target2@example.com");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.grantExpert(user, target.getId()));

        assertEquals("Only experts can grant expert access.", ex.getMessage());
        assertFalse(dao.getAccountByEmail("target2@example.com").isExpert());
    }

    @Test
    void nobodyLoggedInCannotGrantExpertAccess() {
        Account target = addAccount("target3@example.com");

        assertThrows(SecurityException.class, () -> service.grantExpert(null, target.getId()));
        assertFalse(dao.getAccountByEmail("target3@example.com").isExpert());
    }

    @Test
    void grantingToUnknownAccountFails() {
        Account expert = addAccount("expert2@example.com");
        dao.grantExpert(expert.getId());
        Account promoted = dao.getAccountByEmail("expert2@example.com");

        assertThrows(IllegalArgumentException.class, () -> service.grantExpert(promoted, 999999));
    }
}