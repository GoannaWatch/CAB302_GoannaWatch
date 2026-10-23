package GoannaWatch.account.controller;

import GoannaWatch.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Switches the window to another FXML view. */
public final class Navigator {
    private Navigator() {}

    public static void goTo(Node nodeInCurrentScene, String fxml) throws IOException {
        Stage stage = (Stage) nodeInCurrentScene.getScene().getWindow();
        stage.setScene(new Scene(new FXMLLoader(App.class.getResource(fxml)).load()));
    }
}