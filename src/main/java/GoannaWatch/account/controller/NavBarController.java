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
     *
     * @param title the name of the current page
     */
    public void setPageTitle(String title) {
        pageTitleLabel.setText(title);
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
        Navigator.goTo(profileMenu, fxmlName);
    }

    /**
     * Highlights the navigation button for the page being shown.
     *
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
