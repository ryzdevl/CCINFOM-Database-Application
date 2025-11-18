package dao;

import database.DatabaseConnection;
import models.Guest;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    // CREATE - Add new guest with validation
    public Long addGuest(Guest guest) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION STEP 1: Check if email already exists
            String checkSql = "SELECT guest_id FROM guest WHERE email = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, guest.getEmail());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Guest with email " + guest.getEmail() + " already exists!");
            }
            rs.close();
            pstmt.close();

            // INSERT STEP 2: If validation passes, insert the guest
            String insertSql = "INSERT INTO guest (first_name, last_name, phone, email, passport_no) " +
                    "VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, guest.getFirstName());
            pstmt.setString(2, guest.getLastName());
            pstmt.setString(3, guest.getPhone());
            pstmt.setString(4, guest.getEmail());
            pstmt.setString(5, guest.getPassportNo());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating guest failed, no rows affected.");
            }

            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Creating guest failed, no ID obtained.");
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // READ - Get guest by ID
    public Guest getGuestById(Long guestId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM guest WHERE guest_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, guestId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Guest guest = new Guest();
                guest.setGuestId(rs.getLong("guest_id"));
                guest.setFirstName(rs.getString("first_name"));
                guest.setLastName(rs.getString("last_name"));
                guest.setPhone(rs.getString("phone"));
                guest.setEmail(rs.getString("email"));
                guest.setPassportNo(rs.getString("passport_no"));
                return guest;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // READ - Get all guests with optional filtering
    public List<Guest> getAllGuests(String searchTerm) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Guest> guests = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM guest WHERE 1=1";

            if (searchTerm != null && !searchTerm.isEmpty()) {
                sql += " AND (first_name LIKE ? OR last_name LIKE ? OR email LIKE ?)";
            }
            sql += " ORDER BY guest_id";

            pstmt = conn.prepareStatement(sql);

            if (searchTerm != null && !searchTerm.isEmpty()) {
                String pattern = "%" + searchTerm + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                pstmt.setString(3, pattern);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Guest guest = new Guest();
                guest.setGuestId(rs.getLong("guest_id"));
                guest.setFirstName(rs.getString("first_name"));
                guest.setLastName(rs.getString("last_name"));
                guest.setPhone(rs.getString("phone"));
                guest.setEmail(rs.getString("email"));
                guest.setPassportNo(rs.getString("passport_no"));
                guests.add(guest);
            }
            return guests;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // UPDATE - Update guest information with validation
    public boolean updateGuest(Guest guest) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check if email is being changed to an existing one
            String checkSql = "SELECT guest_id FROM guest WHERE email = ? AND guest_id != ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, guest.getEmail());
            pstmt.setLong(2, guest.getGuestId());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Email already in use by another guest!");
            }
            rs.close();
            pstmt.close();

            // UPDATE if validation passes
            String updateSql = "UPDATE guest SET first_name=?, last_name=?, phone=?, email=?, passport_no=? " +
                    "WHERE guest_id=?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, guest.getFirstName());
            pstmt.setString(2, guest.getLastName());
            pstmt.setString(3, guest.getPhone());
            pstmt.setString(4, guest.getEmail());
            pstmt.setString(5, guest.getPassportNo());
            pstmt.setLong(6, guest.getGuestId());

            return pstmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // DELETE - Delete guest with validation
    public boolean deleteGuest(Long guestId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check if guest has active reservations
            String checkSql = "SELECT COUNT(*) FROM reservation WHERE guest_id = ? AND status IN ('confirmed', 'checked-in')";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setLong(1, guestId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete guest with active reservations!");
            }
            rs.close();
            pstmt.close();

            // DELETE if validation passes
            String deleteSql = "DELETE FROM guest WHERE guest_id = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setLong(1, guestId);

            return pstmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // VIEW WITH RELATED RECORDS: Guest with preferences/requests
    public String getGuestWithPreferences(Long guestId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder result = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            // Get guest info
            Guest guest = getGuestById(guestId);
            if (guest == null) {
                return "Guest not found!";
            }

            result.append("GUEST INFORMATION\n");
            result.append("=================\n");
            result.append("Name: ").append(guest.getFullName()).append("\n");
            result.append("Email: ").append(guest.getEmail()).append("\n");
            result.append("Phone: ").append(guest.getPhone()).append("\n\n");

            // Get preferences
            String sql = "SELECT pref_key, pref_value FROM guest_preference WHERE guest_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, guestId);
            rs = pstmt.executeQuery();

            result.append("PREFERENCES/REQUESTS:\n");
            result.append("====================\n");

            boolean hasPreferences = false;
            while (rs.next()) {
                result.append("- ").append(rs.getString("pref_key")).append(": ")
                        .append(rs.getString("pref_value")).append("\n");
                hasPreferences = true;
            }

            if (!hasPreferences) {
                result.append("No preferences recorded.\n");
            }

            return result.toString();
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // VIEW WITH RELATED RECORDS: Guest with feedback and ratings
    public String getGuestWithFeedback(Long guestId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder result = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            Guest guest = getGuestById(guestId);
            if (guest == null) {
                return "Guest not found!";
            }

            result.append("GUEST INFORMATION\n");
            result.append("=================\n");
            result.append("Name: ").append(guest.getFullName()).append("\n\n");

            // Get feedback
            String sql = "SELECT f.rating, f.comments, f.created_at, r.reservation_id " +
                    "FROM feedback f " +
                    "LEFT JOIN reservation r ON f.reservation_id = r.reservation_id " +
                    "WHERE f.guest_id = ? " +
                    "ORDER BY f.created_at DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, guestId);
            rs = pstmt.executeQuery();

            result.append("FEEDBACK & RATINGS:\n");
            result.append("==================\n");

            boolean hasFeedback = false;
            while (rs.next()) {
                result.append("Reservation: ").append(rs.getLong("reservation_id")).append("\n");
                result.append("Rating: ").append(rs.getInt("rating")).append("/5\n");
                result.append("Comments: ").append(rs.getString("comments")).append("\n");
                result.append("Date: ").append(rs.getTimestamp("created_at")).append("\n");
                result.append("---\n");
                hasFeedback = true;
            }

            if (!hasFeedback) {
                result.append("No feedback recorded.\n");
            }

            return result.toString();
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}

