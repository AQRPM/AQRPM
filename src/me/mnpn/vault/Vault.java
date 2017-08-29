package me.mnpn.vault;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import me.mnpn.vault.structs.StructVault;

public class Vault {
	public static final String VERSION = "1.0.0";
	public static StructVault vault;
	public static byte[] key;

	public static final SecureRandom RAND = new SecureRandom();

	public static void main(String[] args) {
		if (args.length >= 1) {
			System.out.println("Yeah.. I don't really do arguments.");
			return;
		}
		Application.launch(VaultLogin.class);
	}

	// public static final Gson GSON = new Gson();
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void loadVault(Path path) throws IOException {
		String content = null;
		content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		Vault.vault = GSON.fromJson(content, StructVault.class);
	}
	public static void saveVault(Path path) throws IOException {
		String content = GSON.toJson(vault, StructVault.class);
		Files.write(path, content.getBytes(StandardCharsets.UTF_8));
	}

	public static String encryptBytes(byte[] data) throws GeneralSecurityException {
		SecretKeySpec spec = new SecretKeySpec(key, "AES");
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(vault.iv));

		return Base64.getEncoder().encodeToString(c.doFinal(data));
	}
	public static byte[] decryptBytes(String data) throws GeneralSecurityException {
		SecretKeySpec spec = new SecretKeySpec(key, "AES");
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, spec, new IvParameterSpec(vault.iv));

		return c.doFinal(Base64.getDecoder().decode(data));
	}

	public static String encrypt(String data) throws GeneralSecurityException {
		return encryptBytes(data.getBytes(StandardCharsets.UTF_8));
	}
	public static String decrypt(String data) throws GeneralSecurityException {
		return new String(decryptBytes(data), StandardCharsets.UTF_8);
	}

	public static byte[] generateIV() throws GeneralSecurityException, NoSuchAlgorithmException {
		byte[] iv = new byte[16];
		RAND.nextBytes(iv);
		return iv;
	}

	public static byte[] hash(String passwordToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		return Arrays.copyOf(md.digest(passwordToHash.getBytes("UTF-8")), 32);
	}

	public static void stacktrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);

		String small = throwable.getMessage();
		String error = sw.getBuffer().toString();

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
		System.exit(0);
	}
}
