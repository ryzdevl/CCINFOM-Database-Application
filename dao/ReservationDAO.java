package dao;

import database.DatabaseConnection;
import models.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    // TRANSACTION 1: Create Reservation with full validation
    public Long createReservation(Reservation reservation, List<Long> amenityIds) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // STEP 1: Validate guest exists
            String guestCheckSql = "SELECT guest_id FROM guest WHERE guest_id = ?";
            pstmt = conn.prepareStatement(guestCheckSql);
            pstmt.setLong(1, reservation.getGuestId());
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Guest ID " + reservation.getGuestId() + " does not exist!");
            }
            rs.close();
            pstmt.close();

            // STEP 2: Validate room availability
            String roomAvailSql = "SELECT status FROM room WHERE room_id = ?";
            pstmt = conn.prepareStatement(roomAvailSql);
            pstmt.setLong(1, reservation.getRoomId());
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Room ID " + reservation.getRoomId() + " does not exist!");
            }

            String roomStatus = rs.getString("status");
            if ("maintenance".equals(roomStatus)) {
                throw new SQLException("Room is under maintenance and cannot be booked!");
            }
            rs.close();
            pstmt.close();

            // STEP 3: Check for overlapping reservations
            String overlapSql = "SELECT COUNT(*) FROM reservation " +
                    "WHERE room_id = ? " +
                    "AND status IN ('confirmed', 'checked-in') " +
                    "AND NOT (check_out <= ? OR check_in >= ?)";
            pstmt = conn.prepareStatement(overlapSql);
            pstmt.setLong(1, reservation.getRoomId());
            pstmt.setDate(2, Date.valueOf(reservation.getCheckIn()));
            pstmt.setDate(3, Date.valueOf(reservation.getCheckOut()));
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Room is already booked for the selected dates!");
            }
            rs.close();
            pstmt.close();

            // STEP 4: Insert reservation
            String insertResSql = "INSERT INTO reservation (guest_id, room_id, check_in, check_out, booking_channel, status) " +
                    "VALUES (?, ?, ?, ?, ?, 'confirmed')";
            pstmt = conn.prepareStatement(insertResSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, reservation.getGuestId());
            pstmt.setLong(2, reservation.getRoomId());
            pstmt.setDate(3, Date.valueOf(reservation.getCheckIn()));
            pstmt.setDate(4, Date.valueOf(reservation.getCheckOut()));
            pstmt.setString(5, reservation.getBookingChannel());

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            Long reservationId = null;
            if (rs.next()) {
                reservationId = rs.getLong(1);
            }
            rs.close();
            pstmt.close();

            // STEP 5: Update room status to 'reserved'
            String updateRoomSql = "UPDATE room SET status = 'reserved' WHERE room_id = ?";
            pstmt = conn.prepareStatement(updateRoomSql);
            pstmt.setLong(1, reservation.getRoomId());
            pstmt.executeUpdate();
            pstmt.close();

            // STEP 6: Link amenities if provided
            if (amenityIds != null && !amenityIds.isEmpty()) {
                String insertAmenitySql = "INSERT INTO reservation_amenity (reservation_id, amenity_id, qty, unit_rate) " +
                        "SELECT ?, amenity_id, 1, rate FROM amenity WHERE amenity_id = ?";
                pstmt = conn.prepareStatement(insertAmenitySql);

                for (Long amenityId : amenityIds) {
                    pstmt.setLong(1, reservationId);
                    pstmt.setLong(2, amenityId);
                    pstmt.executeUpdate();
                }
                pstmt.close();
            }

            conn.commit(); // Commit transaction
            return reservationId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
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

    // TRANSACTION 2: Guest Check-In
    // Assigned to: Charles Andrew Bondoc
    public boolean checkInGuest(Long reservationId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // STEP 1: Retrieve and validate reservation
            String resSql = "SELECT r.*, rm.room_id, rm.status as room_status " +
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
            if ("checked-in".equals(resStatus)) {
                throw new SQLException("Guest is already checked in!");
            }
            if ("checked-out".equals(resStatus)) {
                throw new SQLException("This reservation has already been checked out!");
            }
            if ("cancelled".equals(resStatus)) {
                throw new SQLException("This reservation has been cancelled!");
            }

            Long roomId = rs.getLong("room_id");
            rs.close();
            pstmt.close();

            // STEP 2: Update reservation status to 'checked-in'
            String updateResSql = "UPDATE reservation SET status = 'checked-in' WHERE reservation_id = ?";
            pstmt = conn.prepareStatement(updateResSql);
            pstmt.setLong(1, reservationId);
            pstmt.executeUpdate();
            pstmt.close();

            // STEP 3: Update room status to 'occupied'
            String updateRoomSql = "UPDATE room SET status = 'occupied' WHERE room_id = ?";
            pstmt = conn.prepareStatement(updateRoomSql);
            pstmt.setLong(1, roomId);
            pstmt.executeUpdate();
            pstmt.close();

            // STEP 4: Record check-in event in log
            String logSql = "INSERT INTO checkin_checkout_log (reservation_id, event_type, notes) " +
                    "VALUES (?, 'check-in', 'Guest checked in successfully')";
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

    // Get reservation details for display
    public Reservation getReservationById(Long reservationId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT r.*, g.first_name, g.last_name, rm.room_code " +
                    "FROM reservation r " +
                    "JOIN guest g ON r.guest_id = g.guest_id " +
                    "JOIN room rm ON r.room_id = rm.room_id " +
                    "WHERE r.reservation_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, reservationId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setReservationId(rs.getLong("reservation_id"));
                reservation.setGuestId(rs.getLong("guest_id"));
                reservation.setRoomId(rs.getLong("room_id"));
                reservation.setCheckIn(rs.getDate("check_in").toLocalDate());
                reservation.setCheckOut(rs.getDate("check_out").toLocalDate());
                reservation.setBookingChannel(rs.getString("booking_channel"));
                reservation.setStatus(rs.getString("status"));
                reservation.setGuestName(rs.getString("first_name") + " " + rs.getString("last_name"));
                reservation.setRoomCode(rs.getString("room_code"));
                return reservation;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // List all reservations with filtering
    public List<Reservation> getAllReservations(String statusFilter) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT r.*, g.first_name, g.last_name, rm.room_code " +
                    "FROM reservation r " +
                    "JOIN guest g ON r.guest_id = g.guest_id " +
                    "JOIN room rm ON r.room_id = rm.room_id " +
                    "WHERE 1=1";

            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("All")) {
                sql += " AND r.status = ?";
            }
            sql += " ORDER BY r.check_in DESC";

            pstmt = conn.prepareStatement(sql);

            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("All")) {
                pstmt.setString(1, statusFilter);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setReservationId(rs.getLong("reservation_id"));
                reservation.setGuestId(rs.getLong("guest_id"));
                reservation.setRoomId(rs.getLong("room_id"));
                reservation.setCheckIn(rs.getDate("check_in").toLocalDate());
                reservation.setCheckOut(rs.getDate("check_out").toLocalDate());
                reservation.setBookingChannel(rs.getString("booking_channel"));
                reservation.setStatus(rs.getString("status"));
                reservation.setGuestName(rs.getString("first_name") + " " + rs.getString("last_name"));
                reservation.setRoomCode(rs.getString("room_code"));
                reservations.add(reservation);
            }
            return reservations;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}
