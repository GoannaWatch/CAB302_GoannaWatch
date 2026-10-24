package GoannaWatch.account.controller;

import GoannaWatch.account.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.InputMismatchException;

/**
 * The controller class for the Signup view of the account.App application. This class handles the user interactions in the Signup view.
 */
public class SignupController {

    private final IAccountDAO accountDAO;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label signupFeedbackLabel;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox expertCheck;

    @FXML
    private Hyperlink loginLink;

    /**
     * Constructs a new SignupController with an AccountManager that  uses an in-memory database to perform CRUD operations on accounts.
     */
    public SignupController(){
        this(new SqliteAccountDAO());
    }

    /**
     * Package-private constructor allows test DAO for unit t esting without changing normal app behaviour.
     */
    SignupController(IAccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    /**
     * Displays an inline validation warning on the signup page.
     * @param message The validation message to display
     */
    private void showSignupFeedback(String message) {
        signupFeedbackLabel.setText(message);
        signupFeedbackLabel.setVisible(true);
        signupFeedbackLabel.setManaged(true);
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
     * Handles the action of clicking the signup button. Checks that an account doesn't exist with a matching email and loads the landing view of the application.
     * @throws IOException If the .fxml file for the landing view isn't found.
     */
    @FXML
    private void onSubmitButtonClick() throws IOException {
        signupFeedbackLabel.setVisible(false);
        signupFeedbackLabel.setManaged(false);

        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();

        String email = emailTextField.getText() == null
                ? ""
                : emailTextField.getText().trim();

        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email.isBlank()
                || password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {

            showSignupFeedback("Please fill in all fields.");
            return;
        }

        if (!Account.isPasswordValid(password)) {
            showSignupFeedback(
                    "Password must contain at least 8 characters, an uppercase letter, a number and a special character."
            );
            return;
        }

        if (!password.equals(confirmPassword)){
            showSignupFeedback("Passwords do not match.");
            return;
        }


        try {
            if (isEmailAlreadyRegistered(email)) {
                showSignupFeedback(
                        "An account with that email already exists."
                );
                return;
            }

            Account newAccount = new Account(firstName, lastName, email, password);
            newAccount.setRole(expertCheck.isSelected() ? "expert" : "user");

            accountDAO.addAccount(newAccount);

            Account savedAccount = accountDAO.getAccountByEmail(email);

            if (savedAccount == null) {
                showSignupFeedback(
                        "Account saved, but could not be loaded. Please try logging in."
                );
                return;
            }

            Session.setCurrentAccount(savedAccount);
            Navigator.goTo(submitButton, "landing.fxml");

        } catch (InputMismatchException | IllegalArgumentException e) {
            showSignupFeedback(e.getMessage());

        } catch (RuntimeException e) {
            showSignupFeedback(
                    "Could not access the database. Please try again."
            );
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles selecting the login link.
     * @throws IOException if page cannot be found.
     */
    @FXML
    private void onLoginButtonClick() throws IOException {
        Navigator.goTo(loginLink, "login.fxml");
    }

    /**
     * Checks if an email is already registered with an account in the database.
     * @param email The email to check if it is already registered.
     * @return The account linked to the email if it already exists, else false.
     */
    // Package-private so duplicate email validation can be tested directly
    boolean isEmailAlreadyRegistered(String email) {
        return accountDAO.getAccountByEmail(email) != null;
    }

    /**
     * Initialises the signup page.
     */
    @FXML
    public void initialize() {
        submitButton.setDefaultButton(true);
    }
}

