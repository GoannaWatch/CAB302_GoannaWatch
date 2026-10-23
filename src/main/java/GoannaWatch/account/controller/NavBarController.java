package GoannaWatch.account.controller;

import GoannaWatch.App;
import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the navigation bar shared by every page after login.
 */
public class NavBarController {
    @FXML
    private Label pageTitleLabel;

    @FXML
    private Hyperlink themeButton;

    @FXML
    private Hyperlink logoutButton;

    @FXML
    private Button usersButton;

    /**
     * Sets the theme button's text and shows "Manage Users" only for experts.
     */
    @FXML
    public void initialize() {
        Account current = Session.getCurrentAccount();
        boolean expert = current != null && current.isExpert();
        usersButton.setVisible(expert);
        usersButton.setManaged(expert);

        updateThemeButtonText();
    }

    /**
     * Sets the title shown above the theme, profile and logout links.
     *
     * @param title the name of the current page
     */
    public void setPageTitle(String title) {
        pageTitleLabel.setText(title);
    }

    private void updateThemeButtonText() {
        themeButton.setText(App.isDarkMode() ? "Light Mode" : "Dark Mode");
    }

    @FXML
    private void onThemeButtonClick() {
        App.toggleTheme();
        updateThemeButtonText();
    }

    @FXML
    private void onProfileButtonClick() throws IOException {
        navigateTo("profile.fxml");
    }

    @FXML
    private void onLogoutButtonClick() throws IOException {
        Session.clear();
        navigateTo("welcome.fxml");
    }

    @FXML
    private void onDashboardButtonClick() throws IOException {
        navigateTo("landing.fxml");
    }

    @FXML
    private void onMapButtonClick() throws IOException {
        navigateTo("map.fxml");
    }

    @FXML
    private void onHistoryButtonClick() throws IOException {
        navigateTo("history.fxml");
    }

    @FXML
    private void onEndangeredButtonClick() throws IOException {
        navigateTo("endangered.fxml");
    }

    @FXML
    private void onFavouritesButtonClick() throws IOException {
        navigateTo("favourites.fxml");
    }

    @FXML
    private void onRecordObservationClick() throws IOException {
        navigateTo("record-observation.fxml");
    }

    @FXML
    private void onUsersButtonClick() throws IOException {
        Account account = Session.getCurrentAccount();
        if (account == null || !account.isExpert()) {
            return;
        }
        navigateTo("users.fxml");
    }

    private void navigateTo(String fxmlName) throws IOException {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlName));
        stage.setScene(new Scene(fxmlLoader.load()));
    }

}
