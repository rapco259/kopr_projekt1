package sk.upjs.kopr.file_copy;

import java.awt.event.MouseEvent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import sk.upjs.kopr.file_copy.client.Client;

public class AppController {

	@FXML
	private Button continueButton;

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
	
	private int numberOfTcpConnectionsInt;

	@FXML
	void initialize() {
		fromDirLabel.setText(Constants.FROM_DIR);
		toDirLabel.setText(Constants.TO_DIR);

	}

	@FXML
	void startButtonClicked(ActionEvent event) {
		try {
			numberOfTcpConnectionsInt = Integer.valueOf(numberOfTpcConnetions.getText());
			System.out.println(numberOfTcpConnectionsInt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Client clientManager = new Client();
		clientManager.setNumberOfTcpConnetions(numberOfTcpConnectionsInt);
		clientManager.start();
		
	}
}





