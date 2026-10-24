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

    private final AccountService service;

    /**
     * Constructs a new LoginController with an AccountManager that uses an in-memory database to perform CRUD operations on accounts.
     */
    public LoginController() {
        this(new SqliteAccountDAO());
    }

    /**
     * Package-private constructor allows a test DAO for unit testing without changing normal app behaviour.
     */
    LoginController(IAccountDAO accountDAO) {
        this.service = new AccountService(accountDAO);
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
            Account account = service.authenticate(email, password);

            if (account == null) {
                passwordField.clear();
                showLoginFeedback("Incorrect email or password.");
                return;
            }

            Session.setCurrentAccount(account);
            Navigator.goTo(loginButton, "landing.fxml");

        } catch (RuntimeException e) {
            showLoginFeedback("Could not access the database. Please try again.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles the sign-up link
     * @throws IOException throws IO exception if page cannot be loaded.
     */
    @FXML
    private void onSignupRedirectClick() throws IOException {
        Navigator.goTo(signUpLink, "signup.fxml");
    }

    /**
     * Initialises the view after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        loginButton.setDefaultButton(true);
    }

    /**
     * Finds the account with the given email.
     * @param email the email address to search for
     * @return the matching account
     */
    Optional<Account> findAccountByEmail(String email) {
        List<Account> accounts = service.getAllAccounts();
        return accounts.stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}

