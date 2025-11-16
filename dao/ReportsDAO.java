package dao;

import database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportsDAO {

    // REPORT 1: Room Occupancy Report - Days reserved per month
    // Assigned to: Charles Andrew Bondoc
    public List<String[]> getRoomOccupancyReport(int year, int month) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String[]> reportData = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT rm.room_code, rm.room_type, " +
                    "COALESCE(SUM(DATEDIFF(" +
                    "  LEAST(r.check_out, LAST_DAY(DATE(CONCAT(?, '-', ?, '-01')))), " +
                    "  GREATEST(r.check_in, DATE(CONCAT(?, '-', ?, '-01')))" +
                    ")), 0) as days_reserved " +
                    "FROM room rm " +
                    "LEFT JOIN reservation r ON rm.room_id = r.room_id " +
                    "  AND r.status != 'cancelled' " +
                    "  AND r.check_in <= LAST_DAY(DATE(CONCAT(?, '-', ?, '-01'))) " +
                    "  AND r.check_out >= DATE(CONCAT(?, '-', ?, '-01')) " +
                    "GROUP BY rm.room_id, rm.room_code, rm.room_type " +
                    "ORDER BY rm.room_code";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            pstmt.setInt(4, month);
            pstmt.setInt(5, year);
            pstmt.setInt(6, month);
            pstmt.setInt(7, year);
            pstmt.setInt(8, month);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                        rs.getString("room_code"),
                        rs.getString("room_type"),
                        String.valueOf(rs.getInt("days_reserved"))
                };
                reportData.add(row);
            }

            return reportData;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // REPORT 2: Revenue Report - Total revenue per room per month
    // Assigned to: Ryan James Malapitan
    public List<String[]> getRevenueReport(int year, int month) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String[]> reportData = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT rm.room_code, rm.room_type, rm.rate_per_night, " +
                    "COALESCE(SUM(DATEDIFF(" +
                    "  LEAST(r.check_out, LAST_DAY(DATE(CONCAT(?, '-', ?, '-01')))), " +
                    "  GREATEST(r.check_in, DATE(CONCAT(?, '-', ?, '-01')))" +
                    ") * rm.rate_per_night), 0) as total_revenue " +
                    "FROM room rm " +
                    "LEFT JOIN reservation r ON rm.room_id = r.room_id " +
                    "  AND r.status IN ('checked-in', 'checked-out') " +
                    "  AND r.check_in <= LAST_DAY(DATE(CONCAT(?, '-', ?, '-01'))) " +
                    "  AND r.check_out >= DATE(CONCAT(?, '-', ?, '-01')) " +
                    "GROUP BY rm.room_id, rm.room_code, rm.room_type, rm.rate_per_night " +
                    "ORDER BY total_revenue DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            pstmt.setInt(4, month);
            pstmt.setInt(5, year);
            pstmt.setInt(6, month);
            pstmt.setInt(7, year);
            pstmt.setInt(8, month);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                        rs.getString("room_code"),
                        rs.getString("room_type"),
                        String.format("$%.2f", rs.getDouble("rate_per_night")),
                        String.format("$%.2f", rs.getDouble("total_revenue"))
                };
                reportData.add(row);
            }

            return reportData;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // REPORT 3: Inventory Report - Total items restocked per month
    // Assigned to: Vener Mariano
    public List<String[]> getInventoryReport(int year, int month) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String[]> reportData = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT ii.name, ii.supplier, " +
                    "COALESCE(SUM(r.quantity), 0) as total_restocked, " +
                    "ii.quantity_on_hand as current_quantity " +
                    "FROM inventory_item ii " +
                    "LEFT JOIN restock r ON ii.item_id = r.item_id " +
                    "  AND YEAR(r.restock_date) = ? " +
                    "  AND MONTH(r.restock_date) = ? " +
                    "GROUP BY ii.item_id, ii.name, ii.supplier, ii.quantity_on_hand " +
                    "ORDER BY ii.name";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                        rs.getString("name"),
                        rs.getString("supplier"),
                        String.valueOf(rs.getInt("total_restocked")),
                        String.valueOf(rs.getInt("current_quantity"))
                };
                reportData.add(row);
            }

            return reportData;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // REPORT 4: Amenities Report - Times amenities were availed per month
    // Assigned to: Daniel Pamintuan
    public List<String[]> getAmenitiesReport(int year, int month) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String[]> reportData = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT a.name, a.rate, " +
                    "COUNT(ar.rental_id) as times_rented, " +
                    "COALESCE(SUM(ar.qty), 0) as total_quantity, " +
                    "COALESCE(SUM(ar.qty * ar.rate_per_unit), 0) as total_revenue " +
                    "FROM amenity a " +
                    "LEFT JOIN amenity_rental ar ON a.amenity_id = ar.amenity_id " +
                    "  AND YEAR(ar.rent_start) = ? " +
                    "  AND MONTH(ar.rent_start) = ? " +
                    "GROUP BY a.amenity_id, a.name, a.rate " +
                    "ORDER BY times_rented DESC, a.name";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                        rs.getString("name"),
                        String.format("$%.2f", rs.getDouble("rate")),
                        String.valueOf(rs.getInt("times_rented")),
                        String.valueOf(rs.getInt("total_quantity")),
                        String.format("$%.2f", rs.getDouble("total_revenue"))
                };
                reportData.add(row);
            }

            return reportData;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // BONUS: Get month name for display
    public static String getMonthName(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return (month >= 1 && month <= 12) ? months[month - 1] : "Invalid Month";
    }

    // BONUS: Validate year and month
    public static boolean isValidYearMonth(int year, int month) {
        return year >= 2000 && year <= 2100 && month >= 1 && month <= 12;
    }
}
