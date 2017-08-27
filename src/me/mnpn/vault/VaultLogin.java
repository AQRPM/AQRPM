package me.mnpn.vault;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class VaultLogin extends Application {

	String version = "1.0.0";
	
	public void start(Stage s) {
		
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("AQRPM: Login (v" + version + ")");
		dialog.setHeaderText("Log in to continue");
		dialog.setGraphic(new ImageView(this.getClass().getResource("/pm2-64.png").toString()));

		ButtonType login = new ButtonType("Login", ButtonData.OK_DONE);
		ButtonType register = new ButtonType("Register", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(login, register, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField username = new TextField();
		username.setPromptText("Username");
		PasswordField password = new PasswordField();
		password.setPromptText("Password");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 1, 1);

		Node loginButton = dialog.getDialogPane().lookupButton(login);
		loginButton.setDisable(true);

		username.textProperty().addListener((observable, oldValue, newValue) -> {
		    loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> username.requestFocus());

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == login) {
		        System.out.print(username.getText() + "," + password.getText());
		        try {
					VaultMainMenu.start(s);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		    if (dialogButton == register) {
		    	VaultRegister.register();
		    }
		    return null;
		});

		dialog.showAndWait();
	}
}
