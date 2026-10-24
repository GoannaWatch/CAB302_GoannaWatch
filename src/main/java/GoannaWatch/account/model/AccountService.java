package GoannaWatch.account.model;

import java.util.List;

/**
 * Business rules for accounts.
 */
public class AccountService {

    private final IAccountDAO accountDAO;

    /**
     * @param accountDAO storage for accounts
     */
    public AccountService(IAccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    /**
     * Checks login details.
     * @param email the email entered
     * @param password the password entered
     * @return the matching account, or null if the email or password is wrong
     */
    public Account authenticate(String email, String password) {
        Account account = accountDAO.getAccountByEmail(email);
        if (account == null || !accountDAO.checkPassword(email, password)) {
            return null;
        }
        return account;
    }

    /**
     * @return every account
     */
    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    /**
     * Gives an account the expert role.
     * @param actor the account making the change
     * @param accountId the account to promote
     * @throws SecurityException if the actor is not an expert
     */
    public void grantExpert(Account actor, int accountId) {
        if (actor == null || !actor.isExpert()) {
            throw new SecurityException("Only experts can grant expert access.");
        }
        accountDAO.grantExpert(accountId);
    }
}