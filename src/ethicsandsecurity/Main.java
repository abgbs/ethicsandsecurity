package ethicsandsecurity;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("mainController.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("ECG");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            MainController controller = (MainController) loader.getController();
            if (controller.oGraphStage.isShowing())
                controller.oGraphStage.close();
            if (controller.edGraphStage.isShowing())
                controller.edGraphStage.close();
            primaryStage.close();
        });
        addIconToStage(primaryStage);
        primaryStage.show();
    }

    public static void addIconToStage(Stage stage) {
        stage.getIcons().add(new Image("armstrong.png"));
    }

    public static void main(String[] args) {
        launch(args);
/*         Knapsack ks = new Knapsack();
        try {
            Scanner scr = new Scanner(new File("ECG.txt"));
            ArrayList<Double> data = new ArrayList<>();
            while (scr.hasNextDouble()) {
                data.add(scr.nextDouble());
            }
            data.forEach(e -> {
                //System.out.println(e);
            });
            for(int i = 0; i < 105; i++){
                ks.setValue(data.get(i));
            }
            ks.fillLookup();
            ks.getEncrypted().forEach(e -> {
                System.out.println(e);
            });
        } catch (IOException e) {
            e.printStackTrace();
        } */
    }
}
