package dao;

import database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AmenityRentalDAO {

    // TRANSACTION 5: Amenity/Equipment Rental
    public Long processAmenityRental(Long guestId, Long amenityId, Long reservationId,
                                     LocalDateTime rentStart, LocalDateTime rentEnd,
                                     int quantity) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // STEP 1: Read and confirm guest has active reservation
            String guestCheckSql = "SELECT COUNT(*) FROM reservation WHERE guest_id = ? AND reservation_id = ? AND status = 'checked-in'";
            pstmt = conn.prepareStatement(guestCheckSql);
            pstmt.setLong(1, guestId);
            pstmt.setLong(2, reservationId);
            rs = pstmt.executeQuery();

            if (!rs.next() || rs.getInt(1) == 0) {
                throw new SQLException("Guest must have an active (checked-in) reservation to rent amenities!");
            }
            rs.close();
            pstmt.close();

            // STEP 2: Check availability status of amenity
            String amenitySql = "SELECT availability, rate FROM amenity WHERE amenity_id = ?";
            pstmt = conn.prepareStatement(amenitySql);
            pstmt.setLong(1, amenityId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Amenity not found!");
            }

            String availability = rs.getString("availability");
            if (!"available".equals(availability)) {
                throw new SQLException("Amenity is not available for rental (Status: " + availability + ")!");
            }

            double ratePerUnit = rs.getDouble("rate");
            rs.close();
            pstmt.close();

            // STEP 3: Validate quantity
            if (quantity <= 0) {
                throw new SQLException("Rental quantity must be greater than zero!");
            }

            // STEP 4: Record the rental transaction
            String insertRentalSql = "INSERT INTO amenity_rental (guest_id, amenity_id, reservation_id, rent_start, rent_end, qty, rate_per_unit, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'active')";
            pstmt = conn.prepareStatement(insertRentalSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, guestId);
            pstmt.setLong(2, amenityId);
            pstmt.setLong(3, reservationId);
            pstmt.setTimestamp(4, Timestamp.valueOf(rentStart));
            pstmt.setTimestamp(5, Timestamp.valueOf(rentEnd));
            pstmt.setInt(6, quantity);
            pstmt.setDouble(7, ratePerUnit);

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            Long rentalId = null;
            if (rs.next()) {
                rentalId = rs.getLong(1);
            }
            rs.close();
            pstmt.close();

            // STEP 5: Add charges to reservation billing
            double totalCharge = ratePerUnit * quantity;
            String addChargeSql = "INSERT INTO charge_item (reservation_id, description, qty, unit_price) " +
                    "SELECT ?, CONCAT(a.name, ' Rental'), ?, ? FROM amenity a WHERE a.amenity_id = ?";
            pstmt = conn.prepareStatement(addChargeSql);
            pstmt.setLong(1, reservationId);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, ratePerUnit);
            pstmt.setLong(4, amenityId);
            pstmt.executeUpdate();
            pstmt.close();

            conn.commit();
            return rentalId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                DatabaseConnection.closeConnection(conn);
            }
        }
    }

    // Return/Complete rental
    public boolean returnAmenityRental(Long rentalId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Validate rental exists and is active
            String checkSql = "SELECT status FROM amenity_rental WHERE rental_id = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setLong(1, rentalId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Rental not found!");
            }

            String status = rs.getString("status");
            if (!"active".equals(status)) {
                throw new SQLException("Rental is not active (Status: " + status + ")!");
            }
            rs.close();
            pstmt.close();

            // Update status to returned
            String updateSql = "UPDATE amenity_rental SET status = 'returned' WHERE rental_id = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setLong(1, rentalId);

            return pstmt.executeUpdate() > 0;

        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // Get active rentals for a guest
    public List<String> getActiveRentals(Long guestId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> rentals = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT ar.rental_id, a.name, ar.qty, ar.rent_start, ar.rent_end, ar.status " +
                    "FROM amenity_rental ar " +
                    "JOIN amenity a ON ar.amenity_id = a.amenity_id " +
                    "WHERE ar.guest_id = ? AND ar.status = 'active' " +
                    "ORDER BY ar.rent_start DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, guestId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String entry = String.format("Rental #%d | %s | Qty: %d | Start: %s | End: %s",
                        rs.getLong("rental_id"),
                        rs.getString("name"),
                        rs.getInt("qty"),
                        rs.getTimestamp("rent_start"),
                        rs.getTimestamp("rent_end"));
                rentals.add(entry);
            }

            return rentals;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}

