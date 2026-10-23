package GoannaWatch.account.controller;

import GoannaWatch.account.model.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

/**
 * The controller class for the Welcome view of the account.App application. This class handles the user interactions in the Welcome view.
 */
public class WelcomeController {

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    /**
     * Handles the action of clicking the login button. Loads the Login view of the application.
     * @throws IOException If the .fxml file for the welcome view isn't found.
     */
    @FXML
    protected void onLoginButtonClick() throws IOException {
        Navigator.goTo(loginButton, "login.fxml");
    }

    /**
     * Handles the action of clicking the signup button. Loads the Sign-Up view of the application.
     * @throws IOException If the .fxml file for the welcome view isn't found.
     */
    @FXML
    protected void onSignupButtonClick() throws IOException {
        Navigator.goTo(signupButton, "signup.fxml");
    }
}
