package dao;

import database.DatabaseConnection;
import models.InventoryItem;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    // CREATE
    public Long addInventoryItem(InventoryItem item) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check if item name exists
            String checkSql = "SELECT item_id FROM inventory_item WHERE name = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, item.getName());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Inventory item '" + item.getName() + "' already exists!");
            }
            rs.close();
            pstmt.close();

            // INSERT
            String insertSql = "INSERT INTO inventory_item (name, quantity_on_hand, supplier) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getQuantityOnHand());
            pstmt.setString(3, item.getSupplier());

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get item ID");
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // READ
    public InventoryItem getInventoryItemById(Long itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM inventory_item WHERE item_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, itemId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setItemId(rs.getLong("item_id"));
                item.setName(rs.getString("name"));
                item.setQuantityOnHand(rs.getInt("quantity_on_hand"));
                item.setSupplier(rs.getString("supplier"));
                Date lastRestocked = rs.getDate("last_restocked");
                if (lastRestocked != null) {
                    item.setLastRestocked(lastRestocked.toLocalDate());
                }
                return item;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // LIST
    public List<InventoryItem> getAllInventoryItems() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<InventoryItem> items = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM inventory_item ORDER BY name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setItemId(rs.getLong("item_id"));
                item.setName(rs.getString("name"));
                item.setQuantityOnHand(rs.getInt("quantity_on_hand"));
                item.setSupplier(rs.getString("supplier"));
                Date lastRestocked = rs.getDate("last_restocked");
                if (lastRestocked != null) {
                    item.setLastRestocked(lastRestocked.toLocalDate());
                }
                items.add(item);
            }
            return items;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // UPDATE
    public boolean updateInventoryItem(InventoryItem item) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // VALIDATION: Check name uniqueness
            String checkSql = "SELECT item_id FROM inventory_item WHERE name = ? AND item_id != ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, item.getName());
            pstmt.setLong(2, item.getItemId());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("Item name already in use!");
            }
            rs.close();
            pstmt.close();

            // UPDATE
            String updateSql = "UPDATE inventory_item SET name=?, quantity_on_hand=?, supplier=? WHERE item_id=?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getQuantityOnHand());
            pstmt.setString(3, item.getSupplier());
            pstmt.setLong(4, item.getItemId());

            return pstmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // DELETE
    public boolean deleteInventoryItem(Long itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Can delete inventory items freely (restocking history is cascade deleted)
            String deleteSql = "DELETE FROM inventory_item WHERE item_id = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setLong(1, itemId);

            return pstmt.executeUpdate() > 0;
        } finally {
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }

    // VIEW WITH RELATED RECORDS: Inventory with request count
    public String getInventoryWithRequestCount(Long itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder result = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            InventoryItem item = getInventoryItemById(itemId);
            if (item == null) {
                return "Inventory item not found!";
            }

            result.append("INVENTORY ITEM\n");
            result.append("==============\n");
            result.append("Name: ").append(item.getName()).append("\n");
            result.append("Quantity on Hand: ").append(item.getQuantityOnHand()).append("\n");
            result.append("Supplier: ").append(item.getSupplier()).append("\n\n");

            // Get restock history count
            String sql = "SELECT COUNT(*) as restock_count, SUM(quantity) as total_restocked " +
                    "FROM restock WHERE item_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, itemId);
            rs = pstmt.executeQuery();

            result.append("RESTOCK STATISTICS:\n");
            result.append("===================\n");

            if (rs.next()) {
                result.append("Times Restocked: ").append(rs.getInt("restock_count")).append("\n");
                result.append("Total Quantity Restocked: ").append(rs.getInt("total_restocked")).append("\n");
            }

            return result.toString();
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            DatabaseConnection.closeConnection(conn);
        }
    }
}
