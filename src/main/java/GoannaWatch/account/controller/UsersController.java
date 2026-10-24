package GoannaWatch.account.controller;

import GoannaWatch.account.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

/**
 * Displays users and allows experts to grant expert access.
 */
public class UsersController {

    private final AccountService service;

    @FXML
    private TableView<Account> usersTable;

    @FXML
    private TableColumn<Account, String> nameColumn;

    @FXML
    private TableColumn<Account, String> emailColumn;

    @FXML
    private TableColumn<Account, String> roleColumn;

    @FXML
    private Label messageLabel;

    @FXML private NavBarController navBarController;

    public UsersController() {
        this(new AccountService(new SqliteAccountDAO()));
    }

    UsersController(AccountService service) {
        this.service = service;
    }

    /**
     * Initialises the table and loads users.
     */
    @FXML
    public void initialize() {
        navBarController.setPageTitle("Users");
        navBarController.setActivePage("Users");

        Account account = Session.getCurrentAccount();

        // Only experts can view users.
        if (account == null || !account.isExpert()) {
            usersTable.setDisable(true);
            messageLabel.setText("Only experts can manage users.");
            return;
        }

        // Set the details shown in each column.
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("fullName")
        );
        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );
        roleColumn.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );

        try {
            loadAccounts();
        } catch (RuntimeException e) {
            messageLabel.setText("Could not load users.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads accounts from the database into the table.
     */
    private void loadAccounts() {
        usersTable.getItems().setAll(service.getAllAccounts());
    }

    /**
     * Grants expert access to the selected user.
     */
    @FXML
    private void onGrantExpertClick() {
        Account selectedAccount =
                usersTable.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            messageLabel.setText("Please select a user.");
            return;
        }

        if (selectedAccount.isExpert()) {
            messageLabel.setText("This user is already an expert.");
            return;
        }

        try {
            service.grantExpert(Session.getCurrentAccount(), selectedAccount.getId());

            // Update the role shown in the table.
            selectedAccount.setRole("expert");
            usersTable.refresh();

            messageLabel.setText("Expert access granted.");

        } catch (RuntimeException e) {
            messageLabel.setText("Could not grant expert access.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Returns to the landing page.
     * @throws IOException If the landing view cannot be loaded.
     */
    @FXML
    private void onBackClick() throws IOException {
        Navigator.goTo(usersTable, "landing.fxml");
    }
}