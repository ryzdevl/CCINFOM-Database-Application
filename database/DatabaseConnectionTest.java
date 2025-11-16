package database;

import java.sql.*;

/**
 * Simple test class to verify database connection
 * Run this BEFORE running the GUI to diagnose connection issues
 */
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("Beach Resort Database Connection Test");
        System.out.println("===========================================\n");

        // Test 1: Check MySQL Driver
        System.out.println("Test 1: Checking MySQL JDBC Driver...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL JDBC Driver found!\n");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver NOT found!");
            System.err.println("  Solution: Add mysql-connector-java-8.x.x.jar to your project libraries");
            System.err.println("  Download from: https://dev.mysql.com/downloads/connector/j/\n");
            return;
        }

        // Test 2: Try to connect to MySQL server
        System.out.println("Test 2: Connecting to MySQL server...");
        String url = "jdbc:mysql://localhost:3306/beach_resort";
        String user = "root"; // CHANGE THIS to your MySQL username
        String password = "ChB0770!"; // CHANGE THIS to your MySQL password

        System.out.println("  URL: " + url);
        System.out.println("  User: " + user);
        System.out.println("  Password: " + (password.isEmpty() ? "(empty)" : "****") + "\n");

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Successfully connected to database!\n");

            // Test 3: Check if tables exist
            System.out.println("Test 3: Checking database tables...");
            DatabaseMetaData metaData = conn.getMetaData();
            String[] tables = {"guest", "room", "amenity", "inventory_item", "reservation"};

            for (String table : tables) {
                ResultSet rs = metaData.getTables(null, null, table, null);
                if (rs.next()) {
                    System.out.println("  ✓ Table '" + table + "' exists");
                } else {
                    System.out.println("  ✗ Table '" + table + "' NOT found");
                }
                rs.close();
            }
            System.out.println();

            // Test 4: Try to query guest table
            System.out.println("Test 4: Querying guest table...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM guest");
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("  ✓ Guest table has " + count + " records");
            }
            rs.close();
            stmt.close();

            System.out.println("\n===========================================");
            System.out.println("ALL TESTS PASSED! ✓");
            System.out.println("Your database is ready to use!");
            System.out.println("===========================================");

        } catch (SQLException e) {
            System.err.println("✗ Database connection FAILED!");
            System.err.println("  Error: " + e.getMessage());
            System.err.println("\nCommon Solutions:");
            System.err.println("  1. Make sure MySQL is running");
            System.err.println("  2. Verify database 'beach_resort' exists");
            System.err.println("     Run: CREATE DATABASE beach_resort;");
            System.err.println("  3. Check username/password in this file (lines 21-22)");
            System.err.println("  4. Run the beach_resort.sql script to create tables");
            System.err.println("  5. Check MySQL is running on localhost:3306");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}