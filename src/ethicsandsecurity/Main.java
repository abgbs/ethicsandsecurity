package ethicsandsecurity;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *   @author Ayman Bagabas
 */

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("mainController.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("ECG");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("armstrong.png"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
