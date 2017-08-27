package me.mnpn.vault;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;

import javafx.application.Application;
import me.mnpn.vault.structs.StructVault;

public class Vault {
	public static void main(String[] args) {
		if (args.length >= 1) {
			System.out.println("Yeah.. I don't really do arguments.");
			return;
		}
		StructVault vault = loadVault(Paths.get("C:/Users/mnpn0/Desktop/vault.aqrpm"));
		System.out.println(vault.iv);
		Application.launch(VaultLogin.class);
	}

	public static final Gson GSON = new Gson();

	public static StructVault loadVault(Path path) {
		String content = null;
		try {
			content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return GSON.fromJson(content, StructVault.class);
	}

}
