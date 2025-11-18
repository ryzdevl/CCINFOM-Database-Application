package dao;

import database.DatabaseConnection;
import models.Room;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    // CREATE
    public Long addRoom(Room room) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check if room code already exists
            String checkSql = "SELECT room_id FROM room WHERE room_code = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, room.getRoomCode());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Room code " + room.getRoomCode() + " already exists!");
            }
            rs.close();
            pstmt.close();

            // INSERT
            String insertSql = "INSERT INTO room (room_code, room_type, bed_type, max_capacity, rate_per_night, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, room.getRoomCode());
            pstmt.setString(2, room.getRoomType());
            pstmt.setString(3, room.getBedType());
            pstmt.setInt(4, room.getMaxCapacity());
            pstmt.setDouble(5, room.getRatePerNight());
            pstmt.setString(6, room.getDescription());

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get room ID");
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // READ
    public Room getRoomById(Long roomId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM room WHERE room_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, roomId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getLong("room_id"));
                room.setRoomCode(rs.getString("room_code"));
                room.setRoomType(rs.getString("room_type"));
                room.setBedType(rs.getString("bed_type"));
                room.setMaxCapacity(rs.getInt("max_capacity"));
                room.setRatePerNight(rs.getDouble("rate_per_night"));
                room.setStatus(rs.getString("status"));
                room.setDescription(rs.getString("description"));
                return room;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // LIST with filtering
    public List<Room> getAllRooms(String statusFilter) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM room WHERE 1=1";

            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("All")) {
                sql += " AND status = ?";
            }
            sql += " ORDER BY room_id";

            pstmt = conn.prepareStatement(sql);

            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("All")) {
                pstmt.setString(1, statusFilter);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getLong("room_id"));
                room.setRoomCode(rs.getString("room_code"));
                room.setRoomType(rs.getString("room_type"));
                room.setBedType(rs.getString("bed_type"));
                room.setMaxCapacity(rs.getInt("max_capacity"));
                room.setRatePerNight(rs.getDouble("rate_per_night"));
                room.setStatus(rs.getString("status"));
                room.setDescription(rs.getString("description"));
                rooms.add(room);
            }
            return rooms;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // UPDATE
    public boolean updateRoom(Room room) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check room code uniqueness if changed
            String checkSql = "SELECT room_id FROM room WHERE room_code = ? AND room_id != ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, room.getRoomCode());
            pstmt.setLong(2, room.getRoomId());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Room code already in use!");
            }
            rs.close();
            pstmt.close();

            // UPDATE
            String updateSql = "UPDATE room SET room_code=?, room_type=?, bed_type=?, max_capacity=?, " +
                    "rate_per_night=?, status=?, description=? WHERE room_id=?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, room.getRoomCode());
            pstmt.setString(2, room.getRoomType());
            pstmt.setString(3, room.getBedType());
            pstmt.setInt(4, room.getMaxCapacity());
            pstmt.setDouble(5, room.getRatePerNight());
            pstmt.setString(6, room.getStatus());
            pstmt.setString(7, room.getDescription());
            pstmt.setLong(8, room.getRoomId());

            return pstmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // DELETE
    public boolean deleteRoom(Long roomId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check if room has reservations
            String checkSql = "SELECT COUNT(*) FROM reservation WHERE room_id = ? AND status != 'cancelled'";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setLong(1, roomId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete room with existing reservations!");
            }
            rs.close();
            pstmt.close();

            // DELETE
            String deleteSql = "DELETE FROM room WHERE room_id = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setLong(1, roomId);

            return pstmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // VIEW WITH RELATED RECORDS: Room with service requests
    public String getRoomWithServiceRequests(Long roomId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder result = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            Room room = getRoomById(roomId);
            if (room == null) {
                return "Room not found!";
            }

            result.append("ROOM INFORMATION\n");
            result.append("================\n");
            result.append("Room Code: ").append(room.getRoomCode()).append("\n");
            result.append("Type: ").append(room.getRoomType()).append("\n");
            result.append("Status: ").append(room.getStatus()).append("\n\n");

            // Get service requests (through reservation_amenity)
            String sql = "SELECT a.name, ra.qty, ra.unit_rate, r.reservation_id, g.first_name, g.last_name " +
                    "FROM reservation r " +
                    "JOIN guest g ON r.guest_id = g.guest_id " +
                    "JOIN reservation_amenity ra ON r.reservation_id = ra.reservation_id " +
                    "JOIN amenity a ON ra.amenity_id = a.amenity_id " +
                    "WHERE r.room_id = ? " +
                    "ORDER BY r.reservation_id DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, roomId);
            rs = pstmt.executeQuery();

            result.append("SERVICE REQUESTS:\n");
            result.append("=================\n");

            boolean hasRequests = false;
            while (rs.next()) {
                result.append("Reservation #").append(rs.getLong("reservation_id")).append("\n");
                result.append("Guest: ").append(rs.getString("first_name")).append(" ")
                        .append(rs.getString("last_name")).append("\n");
                result.append("Service: ").append(rs.getString("name")).append("\n");
                result.append("Quantity: ").append(rs.getInt("qty")).append("\n");
                result.append("---\n");
                hasRequests = true;
            }

            if (!hasRequests) {
                result.append("No service requests recorded.\n");
            }

            return result.toString();
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // VIEW WITH RELATED RECORDS: Room with guest count
    public String getRoomWithGuestCount(Long roomId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder result = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            Room room = getRoomById(roomId);
            if (room == null) {
                return "Room not found!";
            }

            result.append("ROOM INFORMATION\n");
            result.append("================\n");
            result.append("Room Code: ").append(room.getRoomCode()).append("\n");
            result.append("Type: ").append(room.getRoomType()).append("\n\n");

            // Get guest count statistics
            String sql = "SELECT COUNT(DISTINCT r.guest_id) as total_guests, " +
                    "COUNT(r.reservation_id) as total_reservations, " +
                    "SUM(DATEDIFF(r.check_out, r.check_in)) as total_nights " +
                    "FROM reservation r " +
                    "WHERE r.room_id = ? AND r.status != 'cancelled'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, roomId);
            rs = pstmt.executeQuery();

            result.append("GUEST STATISTICS:\n");
            result.append("=================\n");

            if (rs.next()) {
                result.append("Total Unique Guests: ").append(rs.getInt("total_guests")).append("\n");
                result.append("Total Reservations: ").append(rs.getInt("total_reservations")).append("\n");
                result.append("Total Nights Booked: ").append(rs.getInt("total_nights")).append("\n");
            }

            return result.toString();
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // Check room availability for date range
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Check if room exists and is not in maintenance
            String statusSql = "SELECT status FROM room WHERE room_id = ?";
            pstmt = conn.prepareStatement(statusSql);
            pstmt.setLong(1, roomId);
            rs = pstmt.executeQuery();

            if (!rs.next() || "maintenance".equals(rs.getString("status"))) {
                return false;
            }
            rs.close();
            pstmt.close();

            // Check for overlapping reservations
            String overlapSql = "SELECT COUNT(*) FROM reservation " +
                    "WHERE room_id = ? " +
                    "AND status IN ('confirmed', 'checked-in') " +
                    "AND NOT (check_out <= ? OR check_in >= ?)";
            pstmt = conn.prepareStatement(overlapSql);
            pstmt.setLong(1, roomId);
            pstmt.setDate(2, Date.valueOf(checkIn));
            pstmt.setDate(3, Date.valueOf(checkOut));
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return false;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}


