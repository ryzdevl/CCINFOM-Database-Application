package dao;

import database.DatabaseConnection;
import java.sql.*;

public class CheckOutDAO {

    // Calculate total charges for a reservation
    public double calculateTotalCharges(Long reservationId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Get room charges
            String roomChargeSql = "SELECT DATEDIFF(r.check_out, r.check_in) * rm.rate_per_night as room_charge " +
                    "FROM reservation r " +
                    "JOIN room rm ON r.room_id = rm.room_id " +
                    "WHERE r.reservation_id = ?";
            pstmt = conn.prepareStatement(roomChargeSql);
            pstmt.setLong(1, reservationId);
            rs = pstmt.executeQuery();

            double roomCharge = 0;
            if (rs.next()) {
                roomCharge = rs.getDouble("room_charge");
            }
            rs.close();
            pstmt.close();

            // Get amenity charges
            String amenityChargeSql = "SELECT SUM(ra.qty * ra.unit_rate) as amenity_charge " +
                    "FROM reservation_amenity ra " +
                    "WHERE ra.reservation_id = ?";
            pstmt = conn.prepareStatement(amenityChargeSql);
            pstmt.setLong(1, reservationId);
            rs = pstmt.executeQuery();

            double amenityCharge = 0;
            if (rs.next()) {
                amenityCharge = rs.getDouble("amenity_charge");
            }
            rs.close();
            pstmt.close();

            // Get additional charges
            String additionalChargeSql = "SELECT SUM(total_price) as additional_charge " +
                    "FROM charge_item " +
                    "WHERE reservation_id = ?";
            pstmt = conn.prepareStatement(additionalChargeSql);
            pstmt.setLong(1, reservationId);
            rs = pstmt.executeQuery();

            double additionalCharge = 0;
            if (rs.next()) {
                additionalCharge = rs.getDouble("additional_charge");
            }

            return roomCharge + amenityCharge + additionalCharge;

        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // checks for duplicate transaction references
    public boolean isTransactionRefUnique(String transactionRef) throws SQLException {
        String sql = "SELECT COUNT(*) " +
                "FROM payment " +
                "WHERE transaction_reference = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, transactionRef);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // true if it doesn't exist
            }
            return true;
        }
    }

    // TRANSACTION 3: Guest Check-Out and Billing Settlement
    public boolean processCheckOut(Long reservationId, double amountPaid, String paymentMethod, String transactionRef) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // STEP 1: Retrieve and validate reservation
            String resSql = "SELECT r.*, rm.room_id " +
                    "FROM reservation r " +
                    "JOIN room rm ON r.room_id = rm.room_id " +
                    "WHERE r.reservation_id = ?";
            pstmt = conn.prepareStatement(resSql);
            pstmt.setLong(1, reservationId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Reservation not found!");
            }

            String resStatus = rs.getString("status");
            if (!"checked-in".equals(resStatus)) {
                throw new SQLException("Guest must be checked in before checking out!");
            }

            Long roomId = rs.getLong("room_id");
            rs.close();
            pstmt.close();

            // STEP 2: Calculate total charges
            double totalCharges = calculateTotalCharges(reservationId);

            if (amountPaid < totalCharges) {
                throw new SQLException("Payment amount (₱" + amountPaid + ") is less than total charges (₱" + totalCharges + ")!");
            }

            // STEP 3: Insert payment record
            String paymentSql = "INSERT INTO payment (reservation_id, amount, method, status, transaction_reference) " +
                    "VALUES (?, ?, ?, 'paid', ?)";
            pstmt = conn.prepareStatement(paymentSql);
            pstmt.setLong(1, reservationId);
            pstmt.setDouble(2, amountPaid);
            pstmt.setString(3, paymentMethod);
            pstmt.setString(4, transactionRef);
            pstmt.executeUpdate();
            pstmt.close();

            // STEP 4: Update reservation status to 'checked-out'
            String updateResSql = "UPDATE reservation SET status = 'checked-out' WHERE reservation_id = ?";
            pstmt = conn.prepareStatement(updateResSql);
            pstmt.setLong(1, reservationId);
            pstmt.executeUpdate();
            pstmt.close();

            // STEP 5: Update room status back to 'available'
            String updateRoomSql = "UPDATE room SET status = 'available' WHERE room_id = ?";
            pstmt = conn.prepareStatement(updateRoomSql);
            pstmt.setLong(1, roomId);
            pstmt.executeUpdate();
            pstmt.close();

            // STEP 6: Record check-out event in log
            String logSql = "INSERT INTO checkin_checkout_log (reservation_id, event_type, notes) " +
                    "VALUES (?, 'check-out', 'Guest checked out and payment settled')";
            pstmt = conn.prepareStatement(logSql);
            pstmt.setLong(1, reservationId);
            pstmt.executeUpdate();
            pstmt.close();

            conn.commit();
            return true;

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

    // Add additional charge item to reservation
    public boolean addChargeItem(Long reservationId, String description, int qty, double unitPrice) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "INSERT INTO charge_item (reservation_id, description, qty, unit_price) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, reservationId);
            pstmt.setString(2, description);
            pstmt.setInt(3, qty);
            pstmt.setDouble(4, unitPrice);

            return pstmt.executeUpdate() > 0;

        } finally {
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}
