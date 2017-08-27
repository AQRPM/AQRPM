package me.mnpn.vault;

import java.io.IOException;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class VaultMainMenu {

	static String version = "1.0.0";
	static String username = "tmp";

	public static void start(Stage s) throws IOException {
		s.setTitle("AQRPM (v" + version + ")");
		s.getIcons().add(new Image("/pm2-512.png"));

		VBox layout = new VBox();
		HBox input = new HBox();
		input.setAlignment(Pos.CENTER);

		GridPane toprow = new GridPane();
		toprow.setAlignment(Pos.CENTER);
		toprow.setHgap(10);
		toprow.setVgap(10);
		toprow.setPadding(new Insets(0, 0, 10, 0));
		
		GridPane bottomrow = new GridPane();
		bottomrow.setAlignment(Pos.BOTTOM_RIGHT);
		bottomrow.setHgap(10);
		bottomrow.setVgap(10);
		bottomrow.setPadding(new Insets(0, 0, 10, 10));

		CheckBox auto = new CheckBox("Checkbox");
		auto.setAlignment(Pos.TOP_LEFT);
		auto.setSelected(true);
		toprow.add(auto, 0, 1);

		Button newPw = new Button("New password");
		Button newCat = new Button("New category");
		toprow.add(newCat, 1, 1);
		toprow.add(newPw, 2, 1);
		layout.getChildren().add(toprow);

		Separator topseparator = new Separator();
		topseparator.setMaxWidth(s.getMaxWidth());
		layout.getChildren().add(topseparator);

		Text title = new Text(" Welcome, " + username);
		title.setFont(Font.font("Ubuntu", FontWeight.NORMAL, 40));
		layout.getChildren().add(title);

		Button logout = new Button("Log out");
		logout.setOnAction(e -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Log out");
			alert.setHeaderText("Logging out");
			alert.setContentText("This will log you out.");
			
			alert.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-information.png"));

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				// Parse JSON changes and restart
			} else {
			    alert.close();
			}
		});
		bottomrow.add(logout, 0, 0);
		
		layout.getChildren().add(bottomrow);
		
		Scene scene = new Scene(layout, 1024, 512);
		s.setScene(scene);
		s.show();
	}
}