package org.example;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
	public static void main(String[] args) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			if (conn != null) {
				System.out.println("Connection is active!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}