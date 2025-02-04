package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLOutput;

public class Main {

	public Main () {

		try (Connection conn = DatabaseConnection.getConnection()) {
			if (conn != null) {
				System.out.println("Connection is active!");


				while (true) {
					printMenu();
					int choice = Integer.parseInt(System.console().readLine());
					switch (choice) {
						case 1:
							System.out.println("Create an account");
							break;
						case 2:
							System.out.println("Log into account");
							break;
						case 0:
							System.out.println("Bye!");
							System.exit(0);
						default:
							System.out.println("Invalid choice");
					}

					


				}




			}
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	public static void main(String[] args) {
		new Main();
	}

	public void printMenu() {

		System.out.println("1. Create an account");
		System.out.println("2. Log into account");
		System.out.println("0. Exit");
		System.out.println();
		System.out.print(">");


	}


}

