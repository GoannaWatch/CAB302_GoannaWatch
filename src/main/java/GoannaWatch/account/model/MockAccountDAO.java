package GoannaWatch.account.model;

import java.util.ArrayList;
import java.util.List;

public class MockAccountDAO implements IAccountDAO {
    /**
     * Static list of accounts to be used as a mock database
     */
    private static final ArrayList<Account> accounts = new ArrayList<>();

    private static int autoIncrementedId = 0;

    public MockAccountDAO() {
        //Add initial accounts to mock database
        addAccount(new Account(
                "John",
                "Doe",
                "johndoe@example.com",
                "Password1!"
        ));

        addAccount(new Account(
                "Jane",
                "Doe",
                "janedoe@example.com",
                "Password1!"
        ));

        addAccount(new Account(
                "Jay",
                "Doe",
                "jaydoe@example.com",
                "Password1!"
        ));
    }

    @Override
    public void addAccount(Account account) {
        account.setId(autoIncrementedId);
        autoIncrementedId++;

        accounts.add(account);
    }

    @Override
    public void updateAccount(Account account) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId() == account.getId()) {
                accounts.set(i, account);
                break;
            }
        }
    }

    @Override
    public void deleteAccount(Account account) {
        accounts.remove(account);
    }

    @Override
    public Account getAccountByEmail(String email) {
        for (Account account : accounts) {
            if (account.getEmail().equalsIgnoreCase(email)) {
                return account;
            }
        }
        return null;
    }

    @Override
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    @Override
    public boolean checkPassword(String email, String password) {
        Account account = getAccountByEmail(email);
        return account != null && account.getPassword() != null
                && account.getPassword().equals(password);
    }

    @Override
    public void grantExpert(int accountId) {
        Account account = getAllAccounts().stream()
                .filter(a -> a.getId() == accountId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        account.setRole("expert");
    }
}