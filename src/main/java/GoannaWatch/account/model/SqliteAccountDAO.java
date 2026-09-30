package GoannaWatch.account.model;

import GoannaWatch.database.DatabaseConnection;
import GoannaWatch.database.DatabaseInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteAccountDAO implements IAccountDAO{

    // Saves an account to the database.
    @Override
    public void addAccount(Account account) {
        String sql = "INSERT INTO accounts "
                + "(first_name, last_name, email, password) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account.getFirstName());
            statement.setString(2, account.getLastName());
            statement.setString(3, account.getEmail());
            // Store the hash instead of the original password.
            String hash = PasswordUtils.hashPassword(account.getPassword());
            statement.setString(4, hash);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add account: " + e.getMessage());
        }
    }

    // Updates account details without changing the password.
    @Override
    public void updateAccount(Account account) {
        String sql = """
            UPDATE accounts
            SET first_name = ?, last_name = ?, email = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account.getFirstName());
            statement.setString(2, account.getLastName());
            statement.setString(3, account.getEmail());
            statement.setInt(4, account.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAccount(Account account) {
        String sql = "DELETE FROM accounts WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, account.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account: " + e.getMessage());
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";

        try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Account account = new Account(
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("email")
                );
                account.setId(result.getInt("id"));
                account.setRole(result.getString("role"));
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all accounts: " + e.getMessage(), e);
        }

        return accounts;
    }


    // Finds an account by email.
    @Override
    public Account getAccountByEmail(String email) {
        String sql = "SELECT * FROM accounts WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    // Load account details without putting a password in the object.
                    Account account = new Account(
                            result.getString("first_name"),
                            result.getString("last_name"),
                            result.getString("email")
                    );

                    account.setId(result.getInt("id"));
                    account.setRole(result.getString("role"));
                    return account;
                }
            }

        }catch (SQLException e) {
            throw new RuntimeException("Failed to get account by email: " + e.getMessage());
        }

        return null;
    }

    // Temporary test for reading an account.
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();

            SqliteAccountDAO dao = new SqliteAccountDAO();

            Account account = dao.getAccountByEmail(
                    "min.test2@example.com"
            );

            if (account != null) {
                System.out.println("ID: " + account.getId());
                System.out.println("Name: " + account.getFullName());
                System.out.println("Email: " + account.getEmail());
            } else {
                System.out.println("Account not found.");
            }

        } catch (SQLException e) {
            System.err.println("Failed to read account: "
                    + e.getMessage());
        }
    }

    /**
     * Checks the password for an account using its stored hash.
     *
     * @param email    the account email
     * @param password the password entered by the user
     * @return true if the account exists and the password matches
     * @throws SQLException if the database query fails
     */

    public boolean checkPassword(String email, String password)
            throws SQLException {

        String sql = "SELECT password FROM accounts WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String hash = result.getString("password");
                    return PasswordUtils.checkPassword(password, hash);
                }
            }
        }

        return false;
    }

    /**
     * Creates the default expert account if it does not already exist.
     * This account provides initial access to the expert functions.
     */
    public void createDefaultExpert() {
        String sql = """
                INSERT OR IGNORE INTO accounts
                (first_name, last_name, email, password, role)
                VALUES (?, ?, ?, ?, 'expert')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "Min");
            statement.setString(2, "Expert");
            statement.setString(3, "expert@123.com");
            statement.setString(
                    4,
                    PasswordUtils.hashPassword("Expert@123")
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to create the default expert account.", e
            );
        }
    }

    /**
     * Grants expert access to the account with the given ID.
     * The currently logged-in account must be an expert.
     *
     * @param accountId the ID of the account receiving expert access
     * @throws SecurityException        if the current user is not an expert
     * @throws IllegalArgumentException if the target account does not exist
     * @throws RuntimeException         if the database update fails
     */
    public void grantExpert(int accountId) {
        Account currentAccount = Session.getCurrentAccount();

        if (currentAccount == null || !currentAccount.isExpert()) {
            throw new SecurityException(
                    "Only experts can grant expert access."
            );
        }

        String sql = """
                UPDATE accounts
                SET role = 'expert'
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Account not found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to grant expert access.", e
            );
        }
    }
}
