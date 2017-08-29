package me.mnpn.vault;

import java.io.IOException;
import java.io.File;

import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Pair;

public class VaultLogin extends Application {
	public void start(Stage s) {
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("AQRPM: Login (v" + Vault.VERSION + ")");
		dialog.setHeaderText("Log in to continue");
		dialog.setGraphic(new ImageView(this.getClass().getResource("/pm2-64.png").toString()));

		ButtonType login = new ButtonType("Login", ButtonData.OK_DONE);
		ButtonType register = new ButtonType("Register", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(login, register, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		PasswordField password = new PasswordField();
		password.setPromptText("Password");

		Button button = new Button("Select vault file");
		AtomicReference<File> file = new AtomicReference<>();
		button.setOnAction(e -> {
			FileChooser chooser = new FileChooser();
			file.set(chooser.showOpenDialog(s));
			// TODO: Use file as vault.
		});

		grid.add(new Label("Vault:"), 0, 0);
		grid.add(button, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 1, 1);

		Node loginButton = dialog.getDialogPane().lookupButton(login);
		loginButton.setDisable(true);

		password.textProperty().addListener((observable, oldValue, newValue) -> {
		    loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> password.requestFocus());

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == login) {
				if (file.get() == null) {
					Text label = new Text("Seriously? You didn't select a fucking file. What the fuck do you want me to log into for you? The void? Fucking idiot. Select a file next time, PLEASE.");
					label.setWrappingWidth(600);
					Alert alert = new Alert(AlertType.WARNING);
					alert.getDialogPane().setContent(label);
					alert.setResizable(true);
					alert.showAndWait();
					return null;
				}
				Vault.vault = Vault.loadVault(file.get().toPath());
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
