package GoannaWatch.account.model;

import GoannaWatch.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Switches the window to another FXML view by replacing root of current scene.
 */
public final class Navigator {
    private Navigator() {}

    /**
     * Loads a view and makes it the root of the scene that contains the given node.
     * @param nodeInCurrentScene any node in the scene being replaced
     * @param fxml the .fxml file to load
     * @throws IOException if the file cannot be loaded
     */
    public static void goTo(Node nodeInCurrentScene, String fxml) throws IOException {
        Scene scene = nodeInCurrentScene.getScene();
        Parent root = new FXMLLoader(App.class.getResource(fxml)).load();
        scene.setRoot(root);
    }
}