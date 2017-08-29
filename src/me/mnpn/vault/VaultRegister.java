package me.mnpn.vault;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Pair;

public class VaultRegister {

	static String version = "1.0.0";

	public static void register() {

		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("AQRPM: Register (v" + version + ")");
		dialog.setHeaderText("Register a new user");
		dialog.setGraphic(new ImageView("/pm2-64.png"));

		ButtonType register = new ButtonType("Register", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(register, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(20);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 0, 10));

		ProgressBar pb = new ProgressBar(0);
		pb.setPrefWidth(375);
		grid.add(pb, 2, 4);

		TextField username = new TextField();
		username.setPromptText("Username");
		PasswordField password = new PasswordField();
		password.setPromptText("Password");
		PasswordField rpassword = new PasswordField();
		rpassword.setPromptText("Repeat Password");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 2, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 2, 1);
		grid.add(new Label("Repeat Password:"), 0, 2);
		grid.add(rpassword, 2, 2);
		Label security = new Label();
		security.setText("Safety of passwords.");
		security.setUnderline(true);
		security.setOnMouseClicked(e -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText("How do we store passwords?");
			alert.setTitle("Password Safety");
			alert.setContentText("We store all passwords locally on your machine, "
					+ "encrypted and all, already done without you having to lift your fucking finger. "
					+ "You're just a lazy asshole, you know? "
					+ "All you have to know is that your passwords are saved, nut.\n\n"
					+ "On a serious note though, we use AES-256 encryption to secure your passwords.");
			alert.showAndWait();
		});
		grid.add(security, 0, 4);
		Label pw = new Label();
		pw.setText("What is a good password?");
		pw.setUnderline(true);
		pw.setOnMouseClicked(e -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText("What's a good password?");
			alert.setTitle("Create a good password");
			alert.setContentText("A good password contains:\n\n"
					+ "1. Many characters. 12, minimum, but we recommend atleast 16.\n"
					+ "2. Numbers, Symbols, UPPERCASE letter, lowercase letters.\n" + "3. No Dictionary Words.\n\n"
					+ "What is a bad password?\n\n"
					+ "Any password that contains names, personal info and dictionary words is very bad.\n\n"
					+ "We both know you're an inconciderate little prick, and you don't know how to choose a half-decent password.\n"
					+ "Go get AQRPC, A quite rude password checker, it might help your tiny brain understand what a good password is.\n");
			Label dl = new Label();
			dl.setText("Download");
			dl.setUnderline(true);
			dl.setOnMouseClicked(event -> {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI("https://git.io/v53HZ"));
					} catch (IOException e2) {
						String error = getStackTrace(e2);
						String small = e2.toString();
						stacktrace(small, error);
						System.exit(0);
					} catch (URISyntaxException e2) {
						String error = getStackTrace(e2);
						String small = e2.toString();
						stacktrace(small, error);
						System.exit(0);
					}
				}
			});
			GridPane expContent = new GridPane();
			expContent.add(dl, 0, 0);
			alert.getDialogPane().setExpandableContent(expContent);
			expContent.setPrefHeight(250);
			alert.showAndWait();
		});
		grid.add(pw, 0, 3);

		Node registerButton = dialog.getDialogPane().lookupButton(register);
		registerButton.setDisable(true);

		username.textProperty().addListener((observable, oldValue, newValue) -> {
			double finalstrength = (updateBar(password.getText().toString()) / 10);
			if (finalstrength < 0.31) {
				pb.setStyle("-fx-accent: red;");
			}
			if (finalstrength > 0.49) {
				pb.setStyle("-fx-accent: orange;");
			}
			if (finalstrength > 0.69) {
				pb.setStyle("-fx-accent: yellow;");
			}
			if (finalstrength > 0.89) {
				pb.setStyle("-fx-accent: green;");
			}
			pb.setProgress(finalstrength);

			registerButton.setDisable(newValue.trim().isEmpty());
			if (username.getText().toString().equals("")) {
				dialog.setHeaderText("The user field cannot be empty.");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png"));
			} else {
				dialog.setHeaderText("Register a new user");
				dialog.setGraphic(new ImageView("/icon64.png"));
			}
		});

		rpassword.textProperty().addListener((observable, oldValue, newValue) -> {
			double finalstrength = (updateBar(password.getText().toString()) / 10);
			if (finalstrength < 0.31) {
				pb.setStyle("-fx-accent: red;");
			}
			if (finalstrength > 0.49) {
				pb.setStyle("-fx-accent: orange;");
			}
			if (finalstrength > 0.69) {
				pb.setStyle("-fx-accent: yellow;");
			}
			if (finalstrength > 0.89) {
				pb.setStyle("-fx-accent: green;");
			}
			pb.setProgress(finalstrength);

			if (username.getText().toString().equals("")) {
				username.setText("Faggot");
			}
			if (password.getText().toString().equals(rpassword.getText().toString())) {
				registerButton.setDisable(false);
				dialog.setHeaderText("Register a new user");
				dialog.setGraphic(new ImageView("/icon64.png"));
			} else {
				registerButton.setDisable(true);
				dialog.setHeaderText(
						username.getText().toString() + ", you do realise that the passwords don't match, do you?");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png"));
			}
			if (password.getText().toString().matches(".*[0-9].*")
					&& !password.getText().toString().matches(".*[a-zA-Z].*")) {
				dialog.setHeaderText("The fuck is wrong with you, idiot? "
						+ "Do you think your fucking phone number is a secure password, or what? "
						+ "Nobody is gonna guess that, right? Wrong. End yourself.");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png"));
			}
		});

		password.textProperty().addListener((observable, oldValue, newValue) -> {
			double finalstrength = (updateBar(password.getText().toString()) / 10);
			if (finalstrength < 0.31) {
				pb.setStyle("-fx-accent: red;");
			}
			if (finalstrength > 0.49) {
				pb.setStyle("-fx-accent: orange;");
			}
			if (finalstrength > 0.69) {
				pb.setStyle("-fx-accent: yellow;");
			}
			if (finalstrength > 0.89) {
				pb.setStyle("-fx-accent: green;");
			}
			pb.setProgress(finalstrength);

			if (username.getText().toString().equals("")) {
				username.setText("Faggot");
			}
			if (password.getText().toString().equals(rpassword.getText().toString())) {
				registerButton.setDisable(false);
				dialog.setHeaderText("Register a new user");
				dialog.setGraphic(new ImageView("/icon64.png"));
			} else {
				registerButton.setDisable(true);
				dialog.setHeaderText(username.getText() + ", you do realise that the passwords don't match, do you?");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png"));
			}
			if (password.getLength() == 1) {
				dialog.setHeaderText(username.getText() + ". You chose this password, didn't you? It's "
						+ "1 fucking character. Seriously. Look at what you've done, " + username.getText()
						+ ". Is this safe?");
				security.setText("Safety of bad passwords.");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png"));
			} else if (password.getLength() < 6) {
				dialog.setHeaderText(username.getText() + ". You chose this password, didn't you? It's "
						+ password.getText().length() + " fucking characters. Seriously. Look at what you've done, "
						+ username.getText() + ". Is this safe?");
				security.setText("Safety of bad passwords.");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png"));
			} else {
				dialog.setHeaderText("Register a new user");
				dialog.setGraphic(new ImageView("/icon64.png"));
				security.setText("Safety of passwords.");
			}
			if (password.getText().toString().startsWith("1234")) {
				dialog.setHeaderText(username.getText()
						+ ", I hope your screen goes black so you can see your reflection on your monitor, "
						+ "just so you can see what a massive failure you are. You chose a password that increments like a pattern. "
						+ password.getText() + ", really?! I could have a baby generate better passwords than that.");
				security.setText("Safety of bad passwords.");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png"));
			} else if (password.getText().toString().matches(".*[0-9].*")
					&& !password.getText().toString().matches(".*[a-zA-Z].*")) {
				dialog.setHeaderText("Your password should not only consist of numbers.");
				dialog.setGraphic(new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-information.png"));
			}
			if (password.getText().toString().length() > 16) {
				dialog.setHeaderText("Register a new user");
				dialog.setGraphic(new ImageView("/icon64.png"));
				security.setText("Safety of passwords.");
			}
		});

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == register) {
				try {
					String hashed = hash(password.getText());
					String encrypted = encrypt(password.getText().getBytes(), hashed.getBytes(), generateIV());
					System.out.println(encrypted);
					try {
						System.out.println(hash(password.getText()));
					} catch (UnsupportedEncodingException e1) {
						String error = getStackTrace(e1);
						String small = e1.toString();
						stacktrace(small, error);
						System.exit(0);
					}
				} catch (GeneralSecurityException e1) {
					String error = getStackTrace(e1);
					String small = e1.toString();
					stacktrace(small, error);
					System.exit(0);
				} catch (UnsupportedEncodingException e2) {
					String error = getStackTrace(e2);
					String small = e2.toString();
					stacktrace(small, error);
					System.exit(0);
				}

				/*try {
					System.out.println(parseJSON("hi"));
				} catch (IOException e1) {
					String error = getStackTrace(e1);
					String small = e1.toString();
					stacktrace(small, error);
				}*/

				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("AQRPM: Register (v" + version + ")");
				alert.setHeaderText("Success!");
				alert.setContentText("User " + username.getText().toString()
						+ " successfully created! You can now log in using your Master Password.");
				alert.showAndWait();
			}
			return null;
		});
		dialog.getDialogPane().setContent(grid);
		dialog.showAndWait();
	}

	public static double updateBar(String password) {
		double finalstrength = 0;
		if (password.length() > 16) {
			finalstrength = finalstrength + 2;
		}
		if (password.length() > 24) {
			finalstrength = finalstrength + 1.5;
		}
		if (password.matches(".*[0-9].*")) {
			finalstrength = finalstrength + 2;
		}
		if (password.matches(".*[a-z].*")) {
			finalstrength = finalstrength + 0.5;
		}
		if (password.matches(".*[A-Z].*")) {
			finalstrength = finalstrength + 2;
		}
		if (!password.matches(".*^[A-Z0-9]+$.*")) { // Oh wow, the moron must
													// have a special character!
			finalstrength = finalstrength + 2;
		}
		return finalstrength;
	}

	private static void stacktrace(String small, String error) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception!");
		alert.setHeaderText("Something went wrong, and it's actually not your fault for once!");
		alert.setContentText(small);
		Label label = new Label("The exception is:");
		TextArea textArea = new TextArea(error);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}

	public static String encrypt(byte[] data, byte[] key, byte[] secretKey) throws GeneralSecurityException {
		String mode = secretKey == null ? "ECB" : "CBC";
		key = Arrays.copyOf(key, 16);
		SecretKeySpec spec = new SecretKeySpec(key, "AES");
		spec = new SecretKeySpec(key, "AES");
		Cipher c = Cipher.getInstance("AES/" + mode + "/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, spec);

		if (secretKey == null)
			c.init(Cipher.ENCRYPT_MODE, spec);
		else
			c.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(secretKey));

		return base64(c.doFinal(data));
	}

	static SecureRandom r = new SecureRandom();

	private static byte[] generateIV() throws NoSuchAlgorithmException {
		Cipher c = null;
		try {
			c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
		SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
		byte[] iv = new byte[c.getBlockSize()];
		randomSecureRandom.nextBytes(iv);
		return iv;
	}

	public static String base64(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static String hash(String passwordToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}
