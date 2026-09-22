package GoannaWatch.account.controller;

import GoannaWatch.App;
import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The controller class for the Landing view of the account.App application. This class handles the user interactions in the Landing Page view.
 */
public class LandingController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Hyperlink logoutButton;

    @FXML
    private Button newObservationButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button mapButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Hyperlink themeButton;

    /**
     * Initialises the controller class. This method is automatically called after the fxml file has been loaded.
     */
    @FXML
    public void initialize(){
        Account current = Session.getCurrentAccount();

        if (current != null) {
            welcomeLabel.setText("Welcome, " + current.getFullName() + "!");
            emailLabel.setText(current.getEmail());
        }

        updateThemeButtonText();
    }

    /**
     * Updates the theme button text to show the theme the user can switch to
     */
    private void updateThemeButtonText() {
        if (App.isDarkMode()) {
            themeButton.setText("Light Mode");
        } else {
            themeButton.setText("Dark Mode");
        }
    }

    /**
     * Switches the application between dark mode and light mode
     */
    @FXML
    private void onThemeButtonClick() {
        App.toggleTheme();
        updateThemeButtonText();
    }

    /**
     * Handles the action of clicking the logout button. Loads the Welcome page view of the application.
     * @throws IOException If the .fxml file for the welcome view isn't found.
     */
    @FXML
    protected void onLogoutButtonClick() throws IOException {
        Session.clear();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("welcome.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    /**
     * Handles the action of clicking the "Record an Observation" button. Loads the Observation view of the application.
     * @throws IOException If the .fxml file for the observation view isn't found.
     */
    @FXML
    protected void onNewObservationButtonClick() throws IOException {
        Stage stage = (Stage) newObservationButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("observation.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    //TODO Create history page
    protected void onHistoryButtonClick() throws IOException {
        Stage stage = (Stage) historyButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("NotImplemented.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    //TODO create Map page
    protected void onMapButtonClick() throws IOException {
        Stage stage = (Stage) mapButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("NotImplemented.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    //TODO Create Dashboard page
    protected void onDashboardButtonClick() throws IOException {
        Stage stage = (Stage) mapButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("NotImplemented.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    // Open the current user's profile.
    @FXML
    private void onProfileButtonClick() throws IOException {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("profile.fxml")
        );
        stage.setScene(new Scene(loader.load()));
    }
}
