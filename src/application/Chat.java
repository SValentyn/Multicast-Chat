package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The application is a MulticastSocket based chat using the JavaFX library.
 * Was used scheme of separation of application data – Model-View-Controller.
 *
 * @author Syniuk Valentyn
 */
public class Chat extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        /* The fxml-file (formed by SceneBuilder'om) is loaded to display the window with the components */
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/form.fxml"));
        root.getStylesheets().add(getClass().getResource("/decor/styles.css").toExternalForm());

        stage.setScene(new Scene(root, 800, 525));
        stage.getIcons().add(new Image(getClass().getResource("/decor/icon.png").toString()));
        stage.setTitle("Chat");
        stage.setX(20);
        stage.setY(20);
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

}

