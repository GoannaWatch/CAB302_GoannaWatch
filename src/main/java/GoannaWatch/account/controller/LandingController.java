package GoannaWatch.account.controller;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.IOException;

/**
 * The controller class for the Landing view of the application. This class handles the user interactions in the Landing Page view.
 */
public class LandingController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private NavBarController navBarController;

    @FXML
    public void initialize() {
        navBarController.setPageTitle("Dashboard");

        Account current = Session.getCurrentAccount();
        if (current != null) {
            welcomeLabel.setText("Welcome, " + current.getFullName() + "!");
            String role = current.isExpert() ? "Expert" : "User";
            emailLabel.setText("[" + role + "] " + current.getEmail());
        }
    }
}
