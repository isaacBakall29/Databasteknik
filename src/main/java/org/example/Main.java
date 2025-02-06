package org.example;

import java.sql.*;
import java.util.Scanner;

public class Main {

	public Main() {
		Scanner sc = new Scanner(System.in);

		try (Connection conn = DatabaseConnection.getConnection()) {
			if (conn != null) {
				while (true) {
					printMenu();
					int choice;
					try {
						choice = Integer.parseInt(sc.nextLine());
					} catch (NumberFormatException e) {
						System.out.println("Invalid input. Please enter a number.");
						continue;
					}

					switch (choice) {
						case 1:
							System.out.println("Username: ");
							String customerName = sc.nextLine();
							System.out.println("Password: ");
							String customerPassword = sc.nextLine();

							int customerId = authenticateUser(conn, customerName, customerPassword);
							if (customerId == -1) {
								System.out.println("Invalid credentials.");
							} else {
								System.out.println("Login successful! User ID: " + customerId);
								new UserMenu(conn, customerId);
							}
							break;

						case 2:
							System.out.println("Greetings, new user. I am your virtual assistant, ready to guide you through the process. Please enter the username you'd like to set. Choose wisely, for this will be your identity in the system");
							String newCustomerName = sc.nextLine();
							System.out.println("Next, to secure your account, please create a password. Make it strong, as this will be the key to your safe haven");
							String newCustomerPassword = sc.nextLine();
							System.out.println("Account successfully created! Welcome to the digital realm. You’re officially in. Now go forth and conquer—just don’t forget your password, we’re not in the business of saving memories");
							int newcustomerId = newUser(conn, newCustomerName, newCustomerPassword);
							break;

						case 0:
							System.out.println("Bye!");
							System.exit(0);

						default:
							System.out.println("Invalid choice. Try again.");
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
		System.out.println("1. Log into account");
		System.out.println("2. Create an account");
		System.out.println("0. Exit");
		System.out.print("> ");
	}

	private int authenticateUser(Connection conn, String username, String password) {
		String query = "SELECT customerId FROM customer WHERE customerName = ? AND customerPassword = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("customerId");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int newUser(Connection conn, String username, String password) {
		String query = "INSERT INTO customer (customerName, customerPassword) VALUES (?, ?)";

		try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);

			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("User created successfully!");
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			} else {
				System.out.println("Failed to create user.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
