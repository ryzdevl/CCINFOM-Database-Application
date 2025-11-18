package dao;

import database.DatabaseConnection;

import java.sql.*;

public class DashboardDAO {

    // 1. Total Guests
    public int getTotalGuests() throws SQLException {
        String sql = "SELECT COUNT(*) FROM guest";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // 2. Active Reservations
    public int getActiveReservations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM room WHERE status = 'reserved'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // 3. Available Rooms
    public int getAvailableRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM room WHERE status = 'available'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // 4. Revenue Today
    public double getRevenueToday() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payment " +
                "WHERE DATE(payment_time) = CURDATE() AND status = 'paid'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    // 5. Occupied Rooms
    public int getOccupiedRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM room WHERE status = 'occupied'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // 6. Amenities Rented
    public int getAmenitiesRented() throws SQLException {
        String sql = "SELECT COUNT(*) FROM amenity_rental WHERE status = 'active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // 7. Inventory Items
    public int getInventoryItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory_item";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // 8. Pending Checkouts
    public int getPendingCheckouts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservation WHERE status = 'checked-in'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
