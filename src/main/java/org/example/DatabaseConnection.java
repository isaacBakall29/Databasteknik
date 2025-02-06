package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

	private static Connection connection;

	static {
		try {
			// Load properties
			Properties properties = new Properties();
			properties.load(new FileInputStream("src/main/java/org/example/config.properties"));

			// Get database credentials
			String url = properties.getProperty("db.url");
			String user = properties.getProperty("db.user");
			String password = properties.getProperty("db.password");

			// Establish connection
			connection = DriverManager.getConnection(url, user, password);
			System.out.println("Database connected successfully!");

		} catch (IOException | SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to connect to database");
		}
	}

	public static Connection getConnection() {
		return connection;
	}
}