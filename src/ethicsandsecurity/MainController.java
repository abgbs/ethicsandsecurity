package ethicsandsecurity;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;

import javafx.event.ActionEvent;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 *   @author Ayman Bagabas
 */

public class MainController implements Initializable {
    @FXML private TextField fileChooserPath;
    @FXML private TabPane pane;
    @FXML private ToggleGroup myToggleGroup;
    @FXML private Toggle rb1;
    @FXML private Toggle rb2;
    @FXML private Tab tab1;
    @FXML private Tab tab2;
    @FXML private Tab tab3;
    @FXML private Button encryptBtn;
    @FXML private Button decryptBtn;
    @FXML private Button saveBtn;
    @FXML private Button quanBtn;
    @FXML public LineChart<Double, Double> graph1;
    @FXML public LineChart<Double, Double> graph2;
    ArrayList<Double> original;
    ArrayList<Double> data;
    ArrayList<Integer> ksquantized;
    ArrayList<Integer> a51quantized;
    Quantizer q;
    Knapsack ks;
    A51 a51;

    @FXML
    public void initialize(URL location, ResourceBundle resource) {
        pane.getTabs().remove(1,3);

        // Select tab1 when closing the other tabs
        tab2.setOnCloseRequest(event -> pane.getSelectionModel().select(0));
        tab3.setOnCloseRequest(event -> pane.getSelectionModel().select(0));

        // Graphs style
        graph1.getXAxis().setLabel("Time (sec)");
        graph1.getYAxis().setLabel("amplitude (mv)");
        graph1.setLegendVisible(false);
        //graph1.setCreateSymbols(true);

        graph2.getXAxis().setLabel("Time (sec)");
        graph2.getYAxis().setLabel("amplitude (mv)");
        graph2.setLegendVisible(false);
        //graph2.setCreateSymbols(false);
    }

    @FXML
    private void handleFileChooserButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text file", "*.txt"),
                                                new FileChooser.ExtensionFilter("ECG data","*.dat"));
        File selectedFile = fileChooser.showOpenDialog(pane.getScene().getWindow());
        fillTable(selectedFile);
    }

    @FXML
    private void handleFileChooserTextField(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            File selectedFile = new File(fileChooserPath.getText());
            fillTable(selectedFile);
        }
    }

    private void fillTable(File file) {
        if (file != null && file.exists() && file.canRead()) {
            // Create arrays
            original = new ArrayList<>();
            data = new ArrayList<>();
            if (file.getName().substring(file.getName().length()-4, file.getName().length()).equals(".txt")) {
                try (Scanner reader = new Scanner(file)) {
                    while (reader.hasNextDouble()) original.add(reader.nextDouble());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                q = new Quantizer();
                ks = new Knapsack(); a51 = new A51();
            } else {
                try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                    original = (ArrayList<Double>) ois.readObject();
                    q = (Quantizer) ois.readObject();
                    ksquantized = (ArrayList<Integer>) ois.readObject();
                    a51quantized = (ArrayList<Integer>) ois.readObject();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Created knapsack and a51 using q
            // Set file path in textfield
            fileChooserPath.setText(file.getAbsolutePath());

            // Create graph and add tab with graph
            //if (pane.getTabs().contains(tab3)) tab3.getOnCloseRequest().handle(null);
            if (pane.getTabs().contains(tab3)) pane.getTabs().remove(2); // remove tab3 when selecting a new file
            graph1.getData().clear();
            graph1.getData().add(getGraphSeries(original));
            addTooltip(graph1);
            tab2.setText("Original data");
            pane.getTabs().add(1, tab2);
            pane.getSelectionModel().select(1);

            // Enable buttons
            encryptBtn.setDisable(false);
            decryptBtn.setDisable(false);
            quanBtn.setDisable(false);
        } else {
            // Disable buttons
            encryptBtn.setDisable(true);
            decryptBtn.setDisable(true);
            saveBtn.setDisable(true);
            quanBtn.setDisable(true);
            // Alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot read file!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleNewQuantizer(ActionEvent event) {
        q = new Quantizer();
        ks = new Knapsack(q);
        a51 = new A51(q);
    }

    @FXML
    private void handleEncrypt(ActionEvent event) {
        if (original == null || original.isEmpty() || data == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File missing!");
            alert.setHeaderText(null);
            alert.setContentText("Please select a file before you encrypt data.");
            alert.showAndWait();
        } else {
            if (rb1.isSelected()) {
                ks = new Knapsack();
                data = ks.encryption(original);
                // Clear table and graph
                graph2.getData().clear();
                // Add knapsack to graph and view tab
                graph2.getData().add(getGraphSeries(data));
                tab3.setText("Encrypted data");
                pane.getTabs().add(2, tab3);
                pane.getSelectionModel().select(2);
                //addTooltip(graph2);
            } else if (rb2.isSelected()) {
                a51 = new A51();
                data = a51.encryption(original);
                graph2.getData().clear();
                graph2.getData().add(getGraphSeries(data));
                tab3.setText("Encrypted data");
                pane.getTabs().add(2, tab3);
                pane.getSelectionModel().select(2);
            }
            // Add tooltip to graph
            addTooltip(graph2);
            // Enable save button
            saveBtn.setDisable(false);
        }
    }

    @FXML
    private void handleDecrypt(ActionEvent event) {
        if (original.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File missing!");
            alert.setHeaderText(null);
            alert.setContentText("Please select a file before you decrypt data.");
            alert.showAndWait();
        } else {
            if (rb1.isSelected()) {
                ks = new Knapsack(q);
                if (ksquantized != null)
                    ks.quantized = new ArrayList<>(ksquantized);
                else
                    ks.encryption(original);
                // Clear table and graph
                graph2.getData().clear();
                data = ks.decryption();
                // Knapsack decryption
                graph2.getData().add(getGraphSeries(data));
                tab3.setText("Decrypted data");
                pane.getTabs().add(2, tab3);
                pane.getSelectionModel().select(2);
                //addTooltip(graph2);
            } else if (rb2.isSelected()) {
                a51 = new A51(q);
                if (a51quantized != null)
                    a51.quantized = new ArrayList<>(a51quantized);
                else
                    a51.encryption(original);
                graph2.getData().clear();
                data = a51.decryption();
                //data.addAll(a51.decryption());data.addAll(a51.decryption());data.addAll(a51.decryption());
                graph2.getData().add(getGraphSeries(data));
                tab3.setText("Decrypted data");
                pane.getTabs().add(2, tab3);
                pane.getSelectionModel().select(2);
            }
            // Add tooltip to graph
            addTooltip(graph2);
            // Enable save button
            saveBtn.setDisable(false);
        }
    }

    private void addTooltip(LineChart<Double, Double> graph) {
        for (XYChart.Series<Double, Double> s : graph.getData()) {
            for (XYChart.Data<Double, Double> d : s.getData()) {
                Node node = d.getNode();
                String text = String.format("(%.0f, %.2f)", d.getXValue(), d.getYValue());
                Tooltip.install(node, new Tooltip(text));
                node.setStyle("-fx-background-color: transparent;");
            }
        }
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ECG data", "*.dat"));
        File file = fileChooser.showSaveDialog(pane.getScene().getWindow());

        if (file != null && ((file.exists() && file.canWrite()) || !file.exists())) {
            // Name extension
            if (!file.getName().substring(file.getName().length()-4, file.getName().length()).equals(".dat"))
                file.renameTo(new File(file.getAbsolutePath() + ".dat"));
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                oos.writeObject(data); // Write results
                if (rb1.isSelected()) {
                    oos.writeObject(ks.q);
                    oos.writeObject(ks.quantized);
                    if (a51 == null) a51 = new A51();
                    oos.writeObject(a51.quantized);
                } else {
                    oos.writeObject(a51.q);
                    if (ks == null) ks = new Knapsack();
                    oos.writeObject(ks.quantized);
                    oos.writeObject(a51.quantized);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot save file!");
            alert.showAndWait();
        }
    }

    private XYChart.Series<Double, Double> getGraphSeries(ArrayList<Double> points) {
        XYChart.Series<Double, Double> series = new XYChart.Series<>();
        double x = 0;
        for (Double n: points) {
            XYChart.Data<Double, Double> point = new XYChart.Data<>(x, n);
            series.getData().add(point);
            x++;
        }

        return series;
    }
}
