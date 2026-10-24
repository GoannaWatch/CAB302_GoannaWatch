package GoannaWatch.account.controller;

import GoannaWatch.App;
import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Navigator;
import GoannaWatch.account.model.Session;
import atlantafx.base.controls.ToggleSwitch;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * Controller for the navigation bar shared by every page after login.
 */
public class NavBarController {
    @FXML
    private Label pageTitleLabel;

    @FXML
    private ToggleSwitch themeToggle;

    @FXML
    private MenuButton profileMenu;

    @FXML
    private Button usersButton;

    @FXML
    private HBox navButtons;

    /**
     * Sets the theme button's text and shows "Manage Users" only for experts.
     */
    @FXML
    public void initialize() {
        Account current = Session.getCurrentAccount();
        boolean expert = current != null && current.isExpert();
        usersButton.setVisible(expert);
        usersButton.setManaged(expert);

        if (current != null) {
            profileMenu.setText(current.getFullName());
        }

        themeToggle.setSelected(App.isDarkMode());
        themeToggle.selectedProperty().addListener((obs, wasDark, isDark) -> {
            if (isDark != App.isDarkMode()) {
                App.toggleTheme();
            }
        });
    }

    /**
     * Sets the title shown above the theme, profile and logout links.
     * @param title the name of the current page
     */
    public void setPageTitle(String title) {
        pageTitleLabel.setText(title);
    }

    /**
     * Opens the profile page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onProfileButtonClick() throws IOException {
        navigateTo("profile.fxml");
    }

    /**
     * Logs the current user out
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onLogoutButtonClick() throws IOException {
        Session.clear();
        navigateTo("welcome.fxml");
    }

    /**
     * Opens the landing page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onDashboardButtonClick() throws IOException {
        navigateTo("landing.fxml");
    }

    /**
     * Opens the map page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onMapButtonClick() throws IOException {
        navigateTo("map.fxml");
    }

    /**
     * Opens the history page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onHistoryButtonClick() throws IOException {
        navigateTo("history.fxml");
    }

    /**
     * Opens the endangered page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onEndangeredButtonClick() throws IOException {
        navigateTo("endangered.fxml");
    }

    /**
     * Opens the favourites page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onFavouritesButtonClick() throws IOException {
        navigateTo("favourites.fxml");
    }

    /**
     * Opens the Record a New Observation page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onRecordObservationClick() throws IOException {
        navigateTo("record-observation.fxml");
    }

    /**
     * Opens the Manage Users page
     * @throws IOException throw IO exception if page cannot be found.
     */
    @FXML
    private void onUsersButtonClick() throws IOException {
        Account account = Session.getCurrentAccount();
        if (account == null || !account.isExpert()) {
            return;
        }
        navigateTo("users.fxml");
    }

    /**
     * Navigates to the given page.
     * @param fxmlName the name of the .fxml file to navigate to.
     * @throws IOException throw IO exception if page cannot be found.
     */
    private void navigateTo(String fxmlName) throws IOException {
        Navigator.goTo(profileMenu, fxmlName);
    }

    /**
     * Highlights the navigation button for the page being shown.
     * @param page the button's userData
     */
    public void setActivePage(String page) {
        for (Node node : navButtons.getChildren()) {
            node.getStyleClass().remove("accent");
            if (page.equals(node.getUserData())) {
                node.getStyleClass().add("accent");
            }
        }
    }

}
