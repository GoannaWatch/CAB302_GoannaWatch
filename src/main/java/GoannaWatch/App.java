package GoannaWatch;

import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import GoannaWatch.database.DatabaseInitializer;
import java.sql.SQLException;
import GoannaWatch.account.model.SqliteAccountDAO;

import java.io.IOException;

/**
 * The main application class for the account.App application. This class extends the JavaFX Application class and starts the JavaFx lifecycle.
 */
public class App extends Application {

    private static boolean darkMode = true;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseInitializer.initialize();

            SqliteAccountDAO accountDAO = new SqliteAccountDAO();
            accountDAO.createDefaultExpert();

        } catch (SQLException e) {
            throw new IOException("Could not initialize the database.", e);
        }

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("welcome.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("GoannaWatch");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Switches the application between dark mode and light mode.
     */

    public static void toggleTheme() {
        darkMode = !darkMode;

        if (darkMode) {
            Application.setUserAgentStylesheet(
                    new PrimerDark().getUserAgentStylesheet()
            );
        } else {
            Application.setUserAgentStylesheet(
                    new PrimerLight().getUserAgentStylesheet()
            );
        }
    }

    /**
     * Returns whether the application is currently using dark mode.
     * @return true if dark mode is active, otherwise false.
     */
    public static boolean isDarkMode() {
        return darkMode;
    }

    /**
     * The main entry point for the application. This method is used to launch the JavaFX application.
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
