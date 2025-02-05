package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class UserMenu {
    private Connection conn;
    private int userId;
    private Scanner scanner = new Scanner(System.in);
    private int orderIdFromCustomerId = -1;

    public UserMenu(Connection conn, int userId) {
        this.conn = conn;
        this.userId = userId;
        startMenu();
    }

    private void startMenu() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs;


            checkOrderCreated();

            clearScreen();

            while (true) {
                System.out.println("\n1. Add product by Id");
                System.out.println("2. View Cart");
                System.out.println("0. Logout");
                System.out.print("Enter choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        // Add product by Id
                        ArrayList<Integer> inStockId = new ArrayList<>();
                        ArrayList<String> shoeNames = new ArrayList<>();

                        checkOrderCreated();
                        rs = stmt.executeQuery("SELECT productName FROM product WHERE productStock > 0");
                        System.out.println("\nAvailable Products:");
                        while (rs.next()) {

                            String shoeName = rs.getString("productName");
                            int id = rs.getInt("productId");
                            shoeNames.add(shoeName);
                            System.out.print(shoeName + ", ");

                            inStockId.add(id);
                        }
                        rs.close();

                        int productId;
                        try {
                            while(true) {
                                System.out.print("Enter shoe name: ");

                                choiceName = scanner.nextLine();
                                if(!inStockId.contains(productId)) {
                                    System.out.println("Invalid product ID. Please enter a valid number.");
                                } else {
                                    break;
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid product ID. Please enter a valid number.");
                            continue;
                        }

                        callAddToCart(productId);

                        break;
                    case 2:
                        checkOrderCreated();
                        // View Cart
                        if (orderIdFromCustomerId == -1) {
                            clearScreen();
                            System.out.println("Cart is empty.");
                            break;
                        }

                        rs = stmt.executeQuery("SELECT productName, productPrice FROM product JOIN orderitems ON product.productId = orderitems.productId WHERE orderId = " + orderIdFromCustomerId);
                        System.out.println("\nCart:");
                        while (rs.next()) {
                            System.out.println("Product Name: " + rs.getString("productName"));
                            System.out.println("Product Price: " + rs.getDouble("productPrice"));
                            System.out.println();
                        }

                        System.out.println("press enter to continue");
                        scanner.nextLine();

                        break;
                    case 0:

                        clearScreen();

                        System.out.println("Bye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void callAddToCart(int productId) {
        String sql = "{CALL AddToCart(?, ?, ?)}";
        try (CallableStatement stmt = conn.prepareCall(sql)) {




            if (orderIdFromCustomerId == -1) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, orderIdFromCustomerId);
            }

            stmt.setInt(2, userId);
            stmt.setInt(3, productId);

            stmt.execute();
            System.out.println("Product added to cart successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void clearScreen() {
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }

    public void checkOrderCreated()  {
        try {
            Statement stmt2 = conn.createStatement();
            ResultSet rs = stmt2.executeQuery("SELECT * FROM orders WHERE customerId = " + userId + " AND orderStatus = 'not payed'");


            while (rs.next()) {
                orderIdFromCustomerId = rs.getInt("orderId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
