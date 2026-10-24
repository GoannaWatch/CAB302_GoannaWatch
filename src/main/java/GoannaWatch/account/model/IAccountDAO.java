package GoannaWatch.account.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for the Account Data Access Object that handles
 * the CRUD operations for the Account class with the database.
 */
public interface IAccountDAO {
    /**
     * Adds a new account to the database.
     *
     * @param account The account to add.
     */
    public void addAccount(Account account);

    /**
     * Updates an existing account in the database.
     *
     * @param account The account to update.
     */
    public void updateAccount(Account account);

    /**
     * Deletes an account from the database.
     *
     * @param account The account to delete.
     */
    public void deleteAccount(Account account);

    /**
     * Retrieves an account from the database.
     *
     * @param email The email of the account to retrieve.
     * @return The account with the given id, or null if not found.
     */
    Account getAccountByEmail(String email);

    /**
     * Checks an account's password
     * @param email the account email
     * @param password the password entered by the user
     * @return true if the users exists and the password matches
     */
    boolean checkPassword(String email, String password);

    /**
     * Gives an account the expert role. Who may do this is decided by AccountService.
     *
     * @param accountId the ID of the account to promote
     * @throws IllegalArgumentException if no account has that ID
     */
    void grantExpert(int accountId);


    /**
     * Retrieves all accounts from the database.
     *
     * @return A list of all accounts in the database.
     */
    public List<Account> getAllAccounts();
}