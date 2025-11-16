package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestockDAO {

    // TRANSACTION 4: Inventory Restocking
    public Long processRestock(Long itemId, String supplier, int quantity, String notes, java.sql.Date restockDate) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // STEP 1: Retrieve current inventory levels
            String itemSql = "SELECT quantity_on_hand FROM inventory_item WHERE item_id = ?";
            pstmt = conn.prepareStatement(itemSql);
            pstmt.setLong(1, itemId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Inventory item not found!");
            }

            int currentQuantity = rs.getInt("quantity_on_hand");
            rs.close();
            pstmt.close();

            // STEP 2: Validate quantity
            if (quantity <= 0) {
                throw new SQLException("Restock quantity must be greater than zero!");
            }

            // STEP 3: Insert restocking record
            String insertRestockSql = "INSERT INTO restock (item_id, supplier, quantity, notes, restock_date) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertRestockSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, itemId);
            pstmt.setString(2, supplier);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, notes);
            pstmt.setDate(5, restockDate);

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            Long restockId = null;
            if (rs.next()) {
                restockId = rs.getLong(1);
            }
            rs.close();
            pstmt.close();

            // STEP 4: Update inventory with new quantity
            int newQuantity = currentQuantity + quantity;
            String updateInventorySql = "UPDATE inventory_item SET quantity_on_hand = ?, last_restocked = ? WHERE item_id = ?";
            pstmt = conn.prepareStatement(updateInventorySql);
            pstmt.setInt(1, newQuantity);
            pstmt.setDate(2, restockDate);
            pstmt.setLong(3, itemId);
            pstmt.executeUpdate();
            pstmt.close();

            conn.commit();
            return restockId;

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

    // Get restock history for an item
    public List<String> getRestockHistory(Long itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> history = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT restock_date, supplier, quantity, notes FROM restock WHERE item_id = ? ORDER BY restock_date DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, itemId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String entry = String.format("Date: %s | Supplier: %s | Qty: %d | Notes: %s",
                        rs.getTimestamp("restock_date"),
                        rs.getString("supplier"),
                        rs.getInt("quantity"),
                        rs.getString("notes"));
                history.add(entry);
            }

            return history;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}
