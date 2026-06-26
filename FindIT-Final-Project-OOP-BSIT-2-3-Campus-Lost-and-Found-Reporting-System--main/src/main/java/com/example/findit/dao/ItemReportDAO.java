package com.example.findit.dao;

import com.example.findit.model.ItemReport;
import com.example.findit.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ItemReportDAO {
    
    public List<ItemReport> findAll() {
        DatabaseBootstrap.ensureApplicationSchema();
        List<ItemReport> reports = new ArrayList<>();
        // FIX 1: Added the WHERE clause to filter out Archived items!
        String sql = """
                SELECT i.item_id, i.item_type, i.item_name, i.description,
                       i.date_lost, i.date_found, i.location, i.image_path, i.tracking_id,
                       c.category_name, u.full_name, u.contact_number
                FROM items i
                JOIN categories c ON c.category_id = i.category_id
                JOIN users u ON u.user_id = i.reporter_id
                WHERE i.record_status = 'Active' OR i.record_status IS NULL
                ORDER BY i.created_at DESC NULLS LAST, i.item_id DESC
                """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reports.add(mapReport(rs));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not load item reports.", e);
        }
        return reports;
    }

    public ItemReport insert(String type, String itemName, String category, String date,
                             String location, String reportedBy, String contact,
                             String description, String imagePath) {
        DatabaseBootstrap.ensureApplicationSchema();
        
        // Generate the tracking ticket
        String newTicket = com.example.findit.util.TrackingGenerator.generateID();
        String sql = """
                INSERT INTO items
                (item_type, item_name, description, date_lost, date_found, location, image_path, status, reporter_id, category_id, tracking_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING item_id, item_type, item_name, description, date_lost, date_found, location, image_path, tracking_id,
                          (SELECT category_name FROM categories WHERE category_id = ?) AS category_name,
                          (SELECT full_name FROM users WHERE user_id = ?) AS full_name,
                          (SELECT contact_number FROM users WHERE user_id = ?) AS contact_number
                """;

        try (Connection conn = DBConnection.connect()) {
            int categoryId = DatabaseBootstrap.ensureCategory(conn, category);
            int reporterId = DatabaseBootstrap.ensureUser(
                    conn,
                    generatedReporterId(contact),
                    reportedBy,
                    contact
            );

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, type);
                stmt.setString(2, itemName);
                stmt.setString(3, description);
                boolean hasDate = date != null && !date.isBlank();
                if ("Lost".equalsIgnoreCase(type)) {
                    if (hasDate) {
                        stmt.setDate(4, Date.valueOf(date));
                    } else {
                        stmt.setNull(4, Types.DATE);
                    }
                    stmt.setNull(5, Types.DATE);
                } else {
                    stmt.setNull(4, Types.DATE);
                    if (hasDate) {
                        stmt.setDate(5, Date.valueOf(date));
                    } else {
                        stmt.setNull(5, Types.DATE);
                    }
                }
                stmt.setString(6, location);
                stmt.setString(7, imagePath);
                stmt.setString(8, "Unclaimed");
                stmt.setInt(9, reporterId);
                stmt.setInt(10, categoryId);
                stmt.setString(11, newTicket);
                
                stmt.setInt(12, categoryId);
                stmt.setInt(13, reporterId);
                stmt.setInt(14, reporterId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return mapReport(rs);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not save item report.", e);
        }

        throw new IllegalStateException("Item report was not saved.");
    }

    public void updateDetails(ItemReport item, String newName, String newLocation, String newDescription) {
        DatabaseBootstrap.ensureApplicationSchema();
        String sql = "UPDATE items SET item_name = ?, location = ?, description = ? WHERE item_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newLocation);
            stmt.setString(3, newDescription);
            stmt.setInt(4, item.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not update item details.", e);
        }
    }

    public void archiveItem(ItemReport item) {
        // FIX 3: Now it archives the item AND its orphaned claims!
        String archiveItemSql = "UPDATE items SET record_status = 'Archived' WHERE item_id = ?";
        String archiveClaimsSql = "UPDATE claims SET record_status = 'Archived' WHERE item_id = ?";
        
        try (java.sql.Connection conn = DBConnection.connect();
             java.sql.PreparedStatement itemStmt = conn.prepareStatement(archiveItemSql);
             java.sql.PreparedStatement claimsStmt = conn.prepareStatement(archiveClaimsSql)) {
            
            // Execute Item Archive
            itemStmt.setInt(1, item.getId());
            itemStmt.executeUpdate();
            
            // Execute Claims Archive
            claimsStmt.setInt(1, item.getId());
            claimsStmt.executeUpdate();
            
        } catch (Exception e) {
            System.err.println("Could not archive item and its claims: " + e.getMessage());
        }
    }

    public void restoreItem(ItemReport item) {
        String restoreItemSql = "UPDATE items SET record_status = 'Active' WHERE item_id = ?";
        String restoreClaimsSql = "UPDATE claims SET record_status = 'Active' WHERE item_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement itemStmt = conn.prepareStatement(restoreItemSql);
             PreparedStatement claimsStmt = conn.prepareStatement(restoreClaimsSql)) {

            itemStmt.setInt(1, item.getId());
            itemStmt.executeUpdate();

            claimsStmt.setInt(1, item.getId());
            claimsStmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not restore item report.", e);
        }
    }

    public List<ItemReport> getArchivedItems() {
        List<ItemReport> archivedList = new ArrayList<>();
        // FIX 2: Added the JOINs so mapReport doesn't crash!
        String sql = """
                SELECT i.item_id, i.item_type, i.item_name, i.description,
                       i.date_lost, i.date_found, i.location, i.image_path, i.tracking_id,
                       c.category_name, u.full_name, u.contact_number
                FROM items i
                JOIN categories c ON c.category_id = i.category_id
                JOIN users u ON u.user_id = i.reporter_id
                WHERE i.record_status = 'Archived'
                ORDER BY i.created_at DESC NULLS LAST, i.item_id DESC
                """;
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                archivedList.add(mapReport(rs)); 
            }
        } catch (Exception e) {
            System.err.println("Error fetching archived items: " + e.getMessage());
        }
        return archivedList;
    }

    private String generatedReporterId(String contact) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = String.valueOf(Math.abs(contact.hashCode()));
        if (suffix.length() > 4) {
            suffix = suffix.substring(0, 4);
        }
        return "REP" + timestamp.substring(Math.max(0, timestamp.length() - 13)) + suffix;
    }

    public void delete(ItemReport item) {
        DatabaseBootstrap.ensureApplicationSchema();
        String deleteClaimsSql = "DELETE FROM claims WHERE item_id = ?";
        String deleteItemSql = "DELETE FROM items WHERE item_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement deleteClaims = conn.prepareStatement(deleteClaimsSql);
             PreparedStatement deleteItem = conn.prepareStatement(deleteItemSql)) {
            deleteClaims.setInt(1, item.getId());
            deleteClaims.executeUpdate();
            deleteItem.setInt(1, item.getId());
            deleteItem.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not delete item report.", e);
        }
    }

    private ItemReport mapReport(ResultSet rs) throws Exception {
        Date dateLost = rs.getDate("date_lost");
        Date dateFound = rs.getDate("date_found");
        String date = dateLost != null ? dateLost.toString() : dateFound != null ? dateFound.toString() : "";
        String itemName = rs.getString("item_name");
        if (itemName == null || itemName.isBlank()) {
            itemName = rs.getString("description");
        }

       return new ItemReport(
                rs.getInt("item_id"),
                rs.getString("item_type"),
                itemName,
                rs.getString("category_name"),
                date,
                rs.getString("location"),
                rs.getString("full_name"),
                rs.getString("contact_number"),
                rs.getString("description"),
                rs.getString("image_path"),
                rs.getString("tracking_id") 
        );
    }
}
