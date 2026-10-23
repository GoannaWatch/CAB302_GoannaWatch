package GoannaWatch.account.controller;

import GoannaWatch.account.model.Account;
import GoannaWatch.account.model.Navigator;
import GoannaWatch.account.model.Session;
import GoannaWatch.observations.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * The controller class for the Landing view of the application. This class handles the user interactions in the Landing Page view.
 */
public class LandingController {

    private final ObservationService service = new ObservationService(
            new SqliteObservationDAO(), new SqliteCommentDAO(), new SqliteFavouriteDAO());

    @FXML private NavBarController navBarController;
    @FXML private Label welcomeLabel, emailLabel;
    @FXML private Label totalLabel, mineLabel, endangeredLabel, favouritesLabel;
    @FXML private Label detailLabel;
    @FXML private ListView<Observation> recentList, endangeredList;

    @FXML
    public void initialize() {
        navBarController.setPageTitle("Dashboard");
        navBarController.setActivePage("Dashboard");
        Account me = Session.getCurrentAccount();
        if (me != null) {
            welcomeLabel.setText("Welcome, " + me.getFullName() + "!");
            emailLabel.setText("[" + (me.isExpert() ? "Expert" : "User") + "] " + me.getEmail());
        }

        List<Observation> all = service.getAll(me);
        List<Observation> endangered = all.stream()
                .filter(o -> "Yes".equalsIgnoreCase(o.getIsEndangered())).toList();

        totalLabel.setText(String.valueOf(all.size()));
        mineLabel.setText(String.valueOf(me == null ? 0 :
                all.stream().filter(o -> o.getObserver().getId() == me.getId()).count()));
        endangeredLabel.setText(String.valueOf(endangered.size()));
        favouritesLabel.setText(String.valueOf(
                all.stream().filter(Observation::getIsFavourite).count()));

        recentList.setCellFactory(list -> new ObservationCell());
        endangeredList.setCellFactory(list -> new ObservationCell());

        recentList.getItems().setAll(all.stream()
                .sorted(Comparator.comparing(Observation::getObservedAt).reversed())
                .limit(5).toList());
        endangeredList.getItems().setAll(endangered.stream().limit(5).toList());

        recentList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showDetail(selected));
        endangeredList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showDetail(selected));
    }

    private void showDetail(Observation o) {
        if (o == null) {
            return;
        }
        StringBuilder text = new StringBuilder()
                .append(o.getAnimalSeen()).append("\n")
                .append(o.getLocation()).append("\n")
                .append(o.getObservedAt()).append("\n")
                .append("Seen by ").append(o.getObserver().getFullName()).append("\n")
                .append("Endangered: ").append(o.getIsEndangered());

        try {
            List<Comment> comments = service.getComments(o.getId());
            if (!comments.isEmpty()) {
                Comment last = comments.get(comments.size() - 1);
                text.append("\n\nLatest comment from ")
                        .append(last.getAuthor().getFullName()).append(": ")
                        .append(last.getText());
            }
        } catch (RuntimeException e) {
            System.err.println("Could not load comments: " + e.getMessage());
        }
        detailLabel.setText(text.toString());
    }

    @FXML private void onQuickRecord() throws IOException { Navigator.goTo(welcomeLabel, "record-observation.fxml"); }
    @FXML private void onQuickHistory() throws IOException { Navigator.goTo(welcomeLabel, "history.fxml"); }
    @FXML private void onQuickMap() throws IOException { Navigator.goTo(welcomeLabel, "map.fxml"); }

    private static class ObservationCell extends ListCell<Observation> {
        @Override
        protected void updateItem(Observation o, boolean empty) {
            super.updateItem(o, empty);
            setText(empty || o == null ? null
                    : o.getAnimalSeen() + ", " + o.getLocation() + " (" + o.getObservedAt() + ")");
        }
    }
}
