package sk.upjs.kopr.file_copy;

import java.awt.event.MouseEvent;
import java.util.concurrent.CountDownLatch;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import sk.upjs.kopr.file_copy.client.Client;

public class AppController {

    @FXML
    private ProgressBar countProgressBar;

    @FXML
    private Label fromDirLabel;

    @FXML
    private TextField numberOfTpcConnetions;

    @FXML
    private Button pauseButton;

    @FXML
    private ProgressBar sizeProgressBar;

    @FXML
    private Button startButton;

    @FXML
    private Label toDirLabel;

    public static int numberOfTcpConnectionsInt;
    public static Client clientManager;
    public CountDownLatch latch;

    @FXML
    void initialize() {
        fromDirLabel.setText(Constants.FROM_DIR);
        toDirLabel.setText(Constants.TO_DIR);
    }

    @FXML
    void startButtonClicked(ActionEvent event) {
        try {
            latch = new CountDownLatch(Integer.parseInt(numberOfTpcConnetions.getText()));
            numberOfTcpConnectionsInt = Integer.parseInt(numberOfTpcConnetions.getText());
            //System.out.println(numberOfTcpConnectionsInt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        clientManager = new Client(numberOfTcpConnectionsInt, latch);
        clientManager.start();
    }

    @FXML
    void pauseButtonClicked(ActionEvent event) {
        clientManager.cancel();

    }
}