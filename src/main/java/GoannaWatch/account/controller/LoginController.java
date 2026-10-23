package GoannaWatch.account.controller;

import GoannaWatch.account.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Initialises the controller class. This method is automatically called after the .fxml file has been loaded.
 */
public class LoginController {

    private final IAccountDAO accountDAO;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginFeedbackLabel;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    private Button loginButton;

    @FXML
    private Button cancelButton;

    /**
     * Constructs a new LoginController with an AccountManager that uses an in-memory database to perform CRUD operations on accounts.
     */
    public LoginController() {
        accountDAO = new  SqliteAccountDAO();
    }

    // Package-private constructor allows a test DAO for unit testing without changing normal app behaviour
    LoginController(IAccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    /**
     * Displays an inline validation warning on the login page
     * @param message The validation message to display
     */
    private void showLoginFeedback(String message) {
        loginFeedbackLabel.setText(message);
        loginFeedbackLabel.setVisible(true);
        loginFeedbackLabel.setManaged(true);
    }

    /**
     * Handles the action of clicking the cancel button. Loads the welcome view of the application.
     * @throws IOException If the .fxml file for the welcome view isn't found.
     */
    @FXML
    private void onCancelButtonClick() throws IOException {
        Navigator.goTo(cancelButton, "welcome.fxml");
    }

    /**
     * Handles the action of clicking the login button. Checks if login details match an existing account and loads the landing view of the application.
     * @throws IOException If the .fxml file for the landing view isn't found.
     */
    @FXML
    private void onNextButtonClick() throws IOException {
        loginFeedbackLabel.setVisible(false);
        loginFeedbackLabel.setManaged(false);

        String email = emailTextField.getText() == null
                ? ""
                : emailTextField.getText().trim();
        String password = passwordField.getText() == null
                ? ""
                : passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showLoginFeedback("Please enter both email and password.");
            return;
        }

        try {
            Account account = accountDAO.getAccountByEmail(email);

            boolean passwordMatches;

            if (accountDAO instanceof SqliteAccountDAO sqliteAccountDAO) {
                passwordMatches = sqliteAccountDAO.checkPassword(email, password);
            } else {
                passwordMatches = account != null
                        && account.getPassword() != null
                        && account.getPassword().equals(password);
            }

            if (account == null || !passwordMatches) {
                showLoginFeedback("Incorrect email or password.");
                return;
            }

            Session.setCurrentAccount(account);
            Navigator.goTo(loginButton, "landing.fxml");

        } catch (SQLException e) {
            showLoginFeedback("Could not access the database. Please try again.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void onSignupRedirectClick() throws IOException {
        Navigator.goTo(signUpLink, "signup.fxml");
    }

    @FXML
    public void initialize() {
        loginButton.setDefaultButton(true);
    }

    Optional<Account> findAccountByEmail(String email) {
        List<Account> accounts = accountDAO.getAllAccounts();
        return accounts.stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}

