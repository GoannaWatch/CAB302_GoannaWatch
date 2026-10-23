package GoannaWatch.account.controller;

import GoannaWatch.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Switches the window to another FXML view.
 *
 */
public final class Navigator {
    private Navigator() {}

    public static void goTo(Node nodeInCurrentScene, String fxml) throws IOException {
        Scene scene = nodeInCurrentScene.getScene();
        Parent root = new FXMLLoader(App.class.getResource(fxml)).load();
        scene.setRoot(root);
    }
}