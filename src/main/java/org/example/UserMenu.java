package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class UserMenu {

    private Connection conn;
    private int userId;
    private JFrame frame;
    private JPanel panel;
    private JTextArea textArea;
    private int orderIdFromCustomerId = -1;

    public UserMenu(Connection conn, int userId) {
        this.conn = conn;
        this.userId = userId;

        initializeUI();
        loadUserMenu();
    }

    private void initializeUI() {
        frame = new JFrame("User Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane);

        JButton addButton = new JButton("Add product");
        JButton viewButton = new JButton("View Cart");
        JButton payButton = new JButton("Pay Cart");
        JButton logoutButton = new JButton("Logout");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addProductToCart();
            }
        });

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewCart();
            }
        });

        payButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                payCart();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        panel.add(addButton);
        panel.add(viewButton);
        panel.add(payButton);
        panel.add(logoutButton);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void loadUserMenu() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs;

            checkOrderCreated();

            StringBuilder menuText = new StringBuilder("<html>");
            menuText.append("<font color='blue'>Welcome!</font><br>");

            menuText.append("<font color='green'><b>Add product:</b> purchase any cool product we have to offer</font><br>");
            menuText.append("<font color='orange'><b>View Cart:</b> Click to see what you want to buy</font><br>");
            menuText.append("<font color='red'><b>Pay Cart:</b> finalize your purchase</font><br>");
            menuText.append("<font color='purple'><b>Logout:</b> Keep your account safe by logging out <3</font><br>");

            menuText.append("</html>");

            JTextPane textArea = new JTextPane();
            textArea.setEditable(false);

            textArea.setContentType("text/html");
            textArea.setText(menuText.toString());


            JScrollPane scrollPane = new JScrollPane(textArea);
            panel.add(scrollPane);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addProductToCart() {
        try {
            ArrayList<String> shoeNames = new ArrayList<>();
            ArrayList<String> shoeDetails = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs;

            checkOrderCreated();

            rs = stmt.executeQuery("SELECT productName, productColor, productSize, productId FROM product WHERE productStock > 0");
            while (rs.next()) {
                String shoeName = rs.getString("productName");
                String shoeColor = rs.getString("productColor");
                int shoeSize = rs.getInt("productSize");
                int productId = rs.getInt("productId");

                String shoeDetail = shoeName + " - " + shoeColor + " - Size: " + shoeSize;
                shoeDetails.add(shoeDetail);
                shoeNames.add(shoeName);
            }

            // Show options in a dialog box
            String choice = (String) JOptionPane.showInputDialog(frame,
                    "Select a shoe:",
                    "Select Shoe",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    shoeDetails.toArray(),
                    shoeDetails.get(0));

            if (choice != null) {
                String[] choiceParts = choice.split(" - ");
                String chosenName = choiceParts[0];
                String chosenColor = choiceParts[1].trim();
                int chosenSize = Integer.parseInt(choiceParts[2].replace("Size:", "").trim());

                rs = stmt.executeQuery("SELECT productId FROM product WHERE productName = '" + chosenName + "' AND productColor = '" + chosenColor + "' AND productSize = " + chosenSize);
                if (rs.next()) {
                    int productId = rs.getInt("productId");
                    callAddToCart(productId);
                } else {
                    JOptionPane.showMessageDialog(frame, "Selected product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "No shoe selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void viewCart() {
        try {
            Statement stmt = conn.createStatement();
            checkOrderCreated();

            if (orderIdFromCustomerId == -1) {
                JOptionPane.showMessageDialog(frame, "Cart is empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ResultSet rs = stmt.executeQuery("SELECT productName, productPrice, productColor, productSize FROM product JOIN orderitems ON product.productId = orderitems.productId WHERE orderId = " + orderIdFromCustomerId);
                StringBuilder cartText = new StringBuilder("Cart:\n");
                while (rs.next()) {
                    cartText.append(rs.getString("productName"))
                            .append(": ").append(rs.getString("productColor"))
                            .append(", ").append(rs.getInt("productSize"))
                            .append(", $").append(rs.getDouble("productPrice"))
                            .append("\n");
                }
                textArea.setText(cartText.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void payCart() {
        try {
            Statement stmt = conn.createStatement();
            checkOrderCreated();

            if (orderIdFromCustomerId == -1) {
                JOptionPane.showMessageDialog(frame, "Cart is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                ResultSet rs = stmt.executeQuery("SELECT SUM(productPrice) FROM product JOIN orderitems ON product.productId = orderitems.productId WHERE orderId = " + orderIdFromCustomerId);

                double total = 0;
                while (rs.next()) {
                    total = rs.getDouble(1);
                }

                int option = JOptionPane.showConfirmDialog(frame, "Total: $" + total + "\nDo you want to proceed with payment?", "Confirm Payment", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    stmt.executeUpdate("UPDATE orders SET orderStatus = 'payed' WHERE orderId = " + orderIdFromCustomerId);
                    orderIdFromCustomerId = -1;
                    JOptionPane.showMessageDialog(frame, "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    textArea.setText("");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        frame.dispose();
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
            JOptionPane.showMessageDialog(frame, "Product added to cart successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkOrderCreated() {
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
