package me.mnpn.vault;

import javafx.application.Application;

public class Vault {
	public static void main(String[] args) {
		if (args.length >= 1) {
			System.out.println("Yeah.. I don't really do arguments.");
			return;
		}
		Application.launch(VaultLogin.class);
	}
}
