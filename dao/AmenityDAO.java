package dao;

import database.DatabaseConnection;
import models.Amenity;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AmenityDAO {

    // CREATE
    public Long addAmenity(Amenity amenity) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check if amenity name exists
            String checkSql = "SELECT amenity_id FROM amenity WHERE name = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, amenity.getName());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Amenity with name '" + amenity.getName() + "' already exists!");
            }
            rs.close();
            pstmt.close();

            // INSERT
            String insertSql = "INSERT INTO amenity (name, description, rate, availability) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, amenity.getName());
            pstmt.setString(2, amenity.getDescription());
            pstmt.setDouble(3, amenity.getRate());
            pstmt.setString(4, amenity.getAvailability());

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get amenity ID");
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // READ
    public Amenity getAmenityById(Long amenityId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM amenity WHERE amenity_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, amenityId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Amenity amenity = new Amenity();
                amenity.setAmenityId(rs.getLong("amenity_id"));
                amenity.setName(rs.getString("name"));
                amenity.setDescription(rs.getString("description"));
                amenity.setRate(rs.getDouble("rate"));
                amenity.setAvailability(rs.getString("availability"));
                amenity.setOverallRating(rs.getDouble("overall_rating"));
                return amenity;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // LIST
    public List<Amenity> getAllAmenities(String availabilityFilter) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Amenity> amenities = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM amenity WHERE 1=1";

            if (availabilityFilter != null && !availabilityFilter.isEmpty() && !availabilityFilter.equals("All")) {
                sql += " AND availability = ?";
            }
            sql += " ORDER BY name";

            pstmt = conn.prepareStatement(sql);

            if (availabilityFilter != null && !availabilityFilter.isEmpty() && !availabilityFilter.equals("All")) {
                pstmt.setString(1, availabilityFilter);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Amenity amenity = new Amenity();
                amenity.setAmenityId(rs.getLong("amenity_id"));
                amenity.setName(rs.getString("name"));
                amenity.setDescription(rs.getString("description"));
                amenity.setRate(rs.getDouble("rate"));
                amenity.setAvailability(rs.getString("availability"));
                amenity.setOverallRating(rs.getDouble("overall_rating"));
                amenities.add(amenity);
            }
            return amenities;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // UPDATE
    public boolean updateAmenity(Amenity amenity) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check name uniqueness
            String checkSql = "SELECT amenity_id FROM amenity WHERE name = ? AND amenity_id != ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, amenity.getName());
            pstmt.setLong(2, amenity.getAmenityId());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Amenity name already in use!");
            }
            rs.close();
            pstmt.close();

            // UPDATE
            String updateSql = "UPDATE amenity SET name=?, description=?, rate=?, availability=? WHERE amenity_id=?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, amenity.getName());
            pstmt.setString(2, amenity.getDescription());
            pstmt.setDouble(3, amenity.getRate());
            pstmt.setString(4, amenity.getAvailability());
            pstmt.setLong(5, amenity.getAmenityId());

            return pstmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // DELETE
    public boolean deleteAmenity(Long amenityId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check if amenity is in use
            String checkSql = "SELECT COUNT(*) FROM amenity_rental WHERE amenity_id = ? AND status = 'active'";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setLong(1, amenityId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete amenity with active rentals!");
            }
            rs.close();
            pstmt.close();

            // DELETE
            String deleteSql = "DELETE FROM amenity WHERE amenity_id = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setLong(1, amenityId);

            return pstmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // VIEW WITH RELATED RECORDS: Amenity with guest requests
    public String getAmenityWithGuestRequests(Long amenityId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder result = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            Amenity amenity = getAmenityById(amenityId);
            if (amenity == null) {
                return "Amenity not found!";
            }

            result.append("AMENITY INFORMATION\n");
            result.append("===================\n");
            result.append("Name: ").append(amenity.getName()).append("\n");
            result.append("Rate: ₱").append(amenity.getRate()).append("\n");
            result.append("Rating: ").append(amenity.getOverallRating()).append("/5\n\n");

            // Get guest request statistics
            String sql = "SELECT COUNT(DISTINCT ar.guest_id) as unique_guests, " +
                    "COUNT(ar.rental_id) as total_rentals, " +
                    "SUM(ar.qty) as total_quantity " +
                    "FROM amenity_rental ar " +
                    "WHERE ar.amenity_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, amenityId);
            rs = pstmt.executeQuery();

            result.append("REQUEST STATISTICS:\n");
            result.append("===================\n");

            if (rs.next()) {
                result.append("Unique Guests: ").append(rs.getInt("unique_guests")).append("\n");
                result.append("Total Rentals: ").append(rs.getInt("total_rentals")).append("\n");
                result.append("Total Quantity Rented: ").append(rs.getInt("total_quantity")).append("\n");
            }

            return result.toString();
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}

