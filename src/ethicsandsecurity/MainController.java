package ethicsandsecurity;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.*;

import javafx.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class MainController {
    @FXML private TextField fileChooserPath;
    @FXML private BorderPane pane;
    @FXML private ToggleGroup myToggleGroup;
    @FXML private Toggle rb1;
    @FXML private Toggle rb2;
    ArrayList<Double> original = new ArrayList<>();
    ArrayList<Double> data;
    ArrayList<String> quantized = new ArrayList<>();
    public Stage oGraphStage = new Stage();
    public Stage edGraphStage = new Stage();
    Knapsack ks = new Knapsack();

    @FXML
    private void handleFileChooserButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text files","*.txt"));
        javafx.stage.Window window = pane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null && selectedFile.exists() && selectedFile.canRead()) {
            dataToArray(selectedFile);
            data = (ArrayList<Double>) (original.clone());
            fileChooserPath.setText(selectedFile.getAbsolutePath());
            showOriginalGraph();
        }
    }

    @FXML
    private void handleFileChooserTextField(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            File selectedFile = new File(fileChooserPath.getText());
            if (selectedFile != null && selectedFile.exists() && selectedFile.canRead()) {
                dataToArray(selectedFile);
                data = (ArrayList<Double>) (original.clone());
                showOriginalGraph();
            } else {
                    fileChooserPath.setText("");
            }
        }
    }

    private void showOriginalGraph() {
        javafx.stage.Window window = pane.getScene().getWindow();
        if (oGraphStage.isShowing())
            oGraphStage.close();
        showGraphWindow(oGraphStage, getGraphSeries(data), "Original data", window.getX()+window.getWidth(), window.getY()-window.getHeight());
    }

    private void showEncDecGraph(String title) {
        if (edGraphStage != null)
            edGraphStage.close();
        showGraphWindow(edGraphStage, getGraphSeries(data), title, oGraphStage.getX(), oGraphStage.getY()+ oGraphStage.getHeight());
    }

    @FXML
    private void handleEncrypt(ActionEvent event) {
        if (!data.isEmpty()) {
            if (rb1.isSelected()) {
                if (original.isEmpty()){
					// show dialog
                } else {
                    for(int i = 0; i < 105; i++){
                        ks.setValue(original.get(i));
                    }
                    ks.fillLookup();
                    ArrayList<Double> encrypted = ks.getEncrypted();
                    data = encrypted;
                    showEncDecGraph("Encrypted data");
                }
            } else if (rb2.isSelected()) {
					// A5/1
            }
        }
    }

    @FXML
    private void handleDecrypt(ActionEvent event) {
        if (rb1.isSelected()) {
            // Knapsack
        } else if (rb2.isSelected()) {
			// A5/1
        }
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text", "*.txt"));
        javafx.stage.Window window = pane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);
        try {
            PrintWriter writer = new PrintWriter(file);
            data.forEach(e -> {
                writer.write(e + "\n");
            });
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Double> getGraphPoints(File file) {
        ArrayList<Double> data = new ArrayList<>();
        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNext()) {
                if (reader.hasNextDouble())
                    data.add(reader.nextDouble());
                else if (reader.hasNextInt())
                    data.add((double)reader.nextInt());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    private <T extends Number> XYChart.Series<Number, Number> getGraphSeries(ArrayList<T> points) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        int x = 0;
        for (T n: points) {
            XYChart.Data<Number, Number> point = new XYChart.Data<>(x, n);
            series.getData().add(point);
            x++;
        }

        return series;
    }

    private void showGraphWindow(Stage stage, XYChart.Series<Number, Number> series, String title, double x, double y, Color color) {

        Pane graphPane = new Pane();
        //series.getNode().setStyle("-fx-stroke: #" + color.toString().substring(4));
        NumberAxis xAxies = new NumberAxis();
        NumberAxis yAxies = new NumberAxis();
        xAxies.setLabel("Time (sec)");
        yAxies.setLabel("amplitude (mv)");
        LineChart<Number,Number> chart = new LineChart<Number, Number>(xAxies, yAxies);
        //chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        chart.getData().add(series);
        for (XYChart.Series<Number, Number> s: chart.getData()) {
            for (XYChart.Data<Number, Number> d: s.getData()) {
                Tooltip.install(d.getNode(), new Tooltip("("+d.getXValue().doubleValue()+", "+d.getYValue().doubleValue()+")"));
            }
        }
        graphPane.getChildren().add(chart);

        stage.setTitle(title);
        stage.setScene(new Scene(graphPane));
        stage.setX(x);
        stage.setY(y);
        stage.setResizable(false);
        stage.setIconified(false);
        Main.addIconToStage(stage);
        stage.show();
    }

    private void showGraphWindow(Stage stage, XYChart.Series series, String title, double x, double y) {
        showGraphWindow(stage, series, title, x, y, Color.ORANGE);
    }

    private void showGraphWindow(Stage stage, XYChart.Series series, String title) {
        showGraphWindow(stage, series, title, oGraphStage.getX(), oGraphStage.getY());
    }

    private void dataToArray(File file) {
        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNext()) {
                if (reader.hasNextDouble())
                    original.add(reader.nextDouble());
                else if (reader.hasNextInt())
                    original.add((double)reader.nextInt());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
