package org.example;

import java.sql.Connection;

public class UserMenu {

    private Connection conn;
    public UserMenu(Connection conn) {

        this.conn = conn;
        startMenu();

    }

    private void startMenu() {

        while (true) {
            int choice = Integer.parseInt(System.console().readLine());

            System.out.println("1. Add product by Id");
            System.out.println("2. Add product by search");

            switch (choice) {
                case 1:
                    break;
                case 2:
                    break;
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice");
            }
        }


    }


}
