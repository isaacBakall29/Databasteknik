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


            while (true) {
                clearScreen();
                System.out.println("\n1. Add product");
                System.out.println("2. View Cart");
                System.out.println("3. Pay Cart");
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
                        clearScreen();

                        ArrayList<String> shoeNames = new ArrayList<>();

                        checkOrderCreated();
                        rs = stmt.executeQuery("SELECT productName FROM product WHERE productStock > 0");
                        System.out.println("\nAvailable Products:");
                        while (rs.next()) {

                            String shoeName = rs.getString("productName");
                            if (shoeNames.contains(shoeName)) {
                                continue;
                            }
                            shoeNames.add(shoeName);
                            System.out.print(shoeName + ", ");

                        }
                        rs.close();

                        String choiceName;
                        try {
                            while(true) {
                                System.out.println();
                                System.out.print("Enter shoe name: ");

                                choiceName = scanner.nextLine();
                                choiceName = choiceName.substring(0, 1).toUpperCase() + choiceName.substring(1).toLowerCase();
                                if(!shoeNames.contains(choiceName)) {
                                    System.out.println("Invalid product name. Please enter a valid name.");
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid product name. Please enter a valid name.");
                            continue;
                        }


                        clearScreen();


                        rs = stmt.executeQuery("SELECT productColor FROM product WHERE productStock > 0 AND productName = '" + choiceName + "'");

                        ArrayList<String> shoeColors = new ArrayList<>();

                        System.out.println("\nAvailable Colors:");
                        while (rs.next()) {

                            String shoeColor = rs.getString("productColor");
                            if (shoeColors.contains(shoeColor)) {
                                continue;
                            }
                            shoeColors.add(shoeColor);
                            System.out.print(shoeColor + ", ");

                        }
                        rs.close();

                        String choiceColor;
                        try {
                            while(true) {

                                System.out.print("\nEnter shoe color: ");
                                choiceColor = scanner.nextLine();
                                choiceColor = choiceColor.substring(0, 1).toUpperCase() + choiceColor.substring(1).toLowerCase();
                                if(!shoeColors.contains(choiceColor)) {
                                    System.out.println("Invalid product color. Please enter a valid color.");
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid product color. Please enter a valid color.");
                            continue;
                        }

                        clearScreen();


                        rs = stmt.executeQuery("SELECT productSize FROM product WHERE productStock > 0 AND productName = '" + choiceName + "' AND productColor = '" + choiceColor + "'");

                        ArrayList<Integer> shoeSizes = new ArrayList<>();
                        System.out.println("\nAvailable Sizes:");
                        while (rs.next()) {
                            int shoeSize = rs.getInt("productSize");
                            if (shoeSizes.contains(shoeSize)) {
                                continue;
                            }
                            shoeSizes.add(shoeSize);
                            System.out.print(shoeSize + ", ");
                        }
                        rs.close();

                        int choiceSize;
                        try {
                            while(true) {
                                System.out.print("\nEnter shoe size: ");
                                choiceSize = Integer.parseInt(scanner.nextLine());
                                if(!shoeSizes.contains(choiceSize)) {
                                    System.out.println("Invalid product size. Please enter a valid size.");
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid product size. Please enter a valid size.");
                            continue;
                        }

                        int productId = -1;

                        rs = stmt.executeQuery("SELECT productId FROM product WHERE productName = '" + choiceName + "' AND productColor = '" + choiceColor + "' AND productSize = " + choiceSize);
                        while (rs.next()) {
                            productId = rs.getInt("productId");
                        }
                        rs.close();

                        callAddToCart(productId);

                        break;
                    case 2:

                        clearScreen();
                        checkOrderCreated();
                        // View Cart
                        if (orderIdFromCustomerId == -1) {
                            clearScreen();
                            System.out.println("Cart is empty.");
                            break;
                        }

                        rs = stmt.executeQuery("SELECT productName, productPrice, productColor, productSize FROM product JOIN orderitems ON product.productId = orderitems.productId WHERE orderId = " + orderIdFromCustomerId);
                        System.out.println("\nCart:");
                        while (rs.next()) {
                            System.out.println(rs.getString("productName") + ": " + rs.getString("productColor") + ", " + rs.getInt("productSize") + ", $" + rs.getDouble("productPrice"));
                            System.out.println();
                        }

                        System.out.println("press enter to continue");
                        scanner.nextLine();

                        break;

                    case 3:
                        clearScreen();
                        checkOrderCreated();
                        if (orderIdFromCustomerId == -1) {
                            System.out.println("Cart is empty.");
                            break;
                        }
                        rs = stmt.executeQuery("SELECT SUM(productPrice) FROM product JOIN orderitems ON product.productId = orderitems.productId WHERE orderId = " + orderIdFromCustomerId);

                        double total = 0;
                        while (rs.next()) {
                            total = rs.getDouble(1);
                        }
                        rs.close();

                        System.out.println("Total: $" + total);

                        stmt.executeUpdate("UPDATE orderstatus SET orderStatus = 'payed' WHERE orderId = " + orderIdFromCustomerId);

                        orderIdFromCustomerId = -1;
                        System.out.println("Order placed successfully!");

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
