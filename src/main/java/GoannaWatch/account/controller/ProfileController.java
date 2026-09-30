package GoannaWatch.account.controller;

import GoannaWatch.App;
import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import GoannaWatch.account.model.SqliteAccountDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.InputMismatchException;

/**
 * Displays and saves the current user's profile.
 */
public class ProfileController {

    private final SqliteAccountDAO accountDAO = new SqliteAccountDAO();

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button backButton;

    // Fill the form with the current user's details.
    @FXML
    public void initialize() {
        Account account = Session.getCurrentAccount();

        if (account != null) {
            firstNameTextField.setText(account.getFirstName());
            lastNameTextField.setText(account.getLastName());
            emailTextField.setText(account.getEmail());
        }
    }

    // Save profile details without changing the password.
    @FXML
    private void onSaveButtonClick() {
        Account currentAccount = Session.getCurrentAccount();

        if (currentAccount == null) {
            feedbackLabel.setText("Please log in first.");
            return;
        }

        try {
            Account updatedAccount = new Account(
                    firstNameTextField.getText().trim(),
                    lastNameTextField.getText().trim(),
                    emailTextField.getText().trim()
            );
            updatedAccount.setId(currentAccount.getId());

            // Keeps the current role when saving the profile.
            updatedAccount.setRole(currentAccount.getRole());

            Account existingAccount = accountDAO.getAccountByEmail(
                    updatedAccount.getEmail()
            );

            if (existingAccount != null
                    && existingAccount.getId() != currentAccount.getId()) {
                feedbackLabel.setText("That email is already in use.");
                return;
            }

            accountDAO.updateAccount(updatedAccount);
            Session.setCurrentAccount(updatedAccount);

            feedbackLabel.setText("Profile saved.");

        } catch (InputMismatchException e) {
            feedbackLabel.setText(e.getMessage());

        } catch (RuntimeException e) {
            feedbackLabel.setText("Could not save your profile. Please try again.");
            System.err.println(e.getMessage());
        }
    }

    // Return to the home page.
    @FXML
    private void onBackButtonClick() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("landing.fxml")
        );
        stage.setScene(new Scene(loader.load()));
    }
}