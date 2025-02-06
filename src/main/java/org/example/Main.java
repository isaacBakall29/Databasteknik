package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Main {
	private JFrame frame;
	private Connection conn;

	public Main() {
		conn = DatabaseConnection.getConnection();

		if (conn != null) {
			frame = new JFrame("Login Menu");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(400, 300);
			frame.setLayout(new FlowLayout());

			JButton loginButton = new JButton("Login");
			JButton createAccountButton = new JButton("Create Account");
			JButton exitButton = new JButton("Exit");

			frame.add(loginButton);
			frame.add(createAccountButton);
			frame.add(exitButton);

			loginButton.addActionListener(e -> showLoginForm());
			createAccountButton.addActionListener(e -> showCreateAccountForm());
			exitButton.addActionListener(e -> System.exit(0));

			frame.setVisible(true);
		}
	}

	private void showLoginForm() {
		JTextField usernameField = new JTextField(15);
		JPasswordField passwordField = new JPasswordField(15);

		Object[] message = {
				"Username:", usernameField,
				"Password:", passwordField
		};

		int option = JOptionPane.showConfirmDialog(frame, message, "Login", JOptionPane.OK_CANCEL_OPTION);

		if (option == JOptionPane.OK_OPTION) {
			String username = usernameField.getText();
			String password = new String(passwordField.getPassword());

			int customerId = authenticateUser(conn, username, password);
			if (customerId != -1) {
				new UserMenu(conn, customerId);
				frame.dispose();
			} else {
				JOptionPane.showMessageDialog(frame, "Invalid credentials.");
			}
		}
	}

	private void showCreateAccountForm() {
		JTextField usernameField = new JTextField(15);
		JPasswordField passwordField = new JPasswordField(15);

		Object[] message = {
				"Username:", usernameField,
				"Password:", passwordField
		};

		int option = JOptionPane.showConfirmDialog(frame, message, "Create Account", JOptionPane.OK_CANCEL_OPTION);

		if (option == JOptionPane.OK_OPTION) {
			String username = usernameField.getText();
			String password = new String(passwordField.getPassword());
			int newCustomerId = newUser(conn, username, password);
			if (newCustomerId != -1) {
				JOptionPane.showMessageDialog(frame, "Account created successfully!");
			} else {
				JOptionPane.showMessageDialog(frame, "Failed to create account.");
			}
		}
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
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static void main(String[] args) {
		new Main();
	}
}
