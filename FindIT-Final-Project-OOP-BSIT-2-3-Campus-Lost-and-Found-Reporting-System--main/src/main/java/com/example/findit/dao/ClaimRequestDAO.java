package com.example.findit.dao;

import com.example.findit.model.ClaimRequest;
import com.example.findit.model.ItemReport;
import com.example.findit.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClaimRequestDAO {
    public List<ClaimRequest> findAll() {
        DatabaseBootstrap.ensureApplicationSchema();
        List<ClaimRequest> claims = new ArrayList<>();
        
        // FIX 1: Added the WHERE clause to hide Archived claims from the main feed
        String sql = """
                SELECT cl.claim_id, cl.claim_status, cl.claimant_name, cl.student_number,
                       cl.contact_info, cl.proof_description, cl.tracking_id,
                       claimant.full_name AS claimant_user_name, claimant.id_number AS claimant_id_number,
                       claimant.contact_number AS claimant_contact,
                       i.item_id, i.item_type, i.item_name, i.description, i.date_lost, i.date_found,
                       i.location, i.image_path, i.tracking_id AS item_tracking_id, c.category_name,
                       reporter.full_name AS reporter_name, reporter.contact_number AS reporter_contact
                FROM claims cl
                JOIN items i ON i.item_id = cl.item_id
                JOIN categories c ON c.category_id = i.category_id
                JOIN users reporter ON reporter.user_id = i.reporter_id
                JOIN users claimant ON claimant.user_id = cl.claimant_id
                WHERE cl.record_status = 'Active' OR cl.record_status IS NULL
                ORDER BY cl.claim_date DESC NULLS LAST, cl.claim_id DESC
                """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                claims.add(mapClaim(rs));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not load claim requests.", e);
        }
        return claims;
    }

    public ClaimRequest insert(ItemReport item, String claimantName, String studentNumber,
                               String contactInfo, String proofDescription) {
        return insert(item, claimantName, studentNumber, contactInfo, proofDescription, "Pending");
    }

    public ClaimRequest insert(ItemReport item, String claimantName, String studentNumber,
                               String contactInfo, String proofDescription, String status) {
        DatabaseBootstrap.ensureApplicationSchema();
        
        String newTicket = com.example.findit.util.TrackingGenerator.generateID();
        String sql = """
                INSERT INTO claims
                (item_id, claimant_id, claim_date, claim_status, claimant_name, student_number, contact_info, proof_description, tracking_id)
                VALUES (?, ?, (timezone('Asia/Manila', now()))::date, ?, ?, ?, ?, ?, ?)
                RETURNING claim_id
                """;

        try (Connection conn = DBConnection.connect()) {
            String storedStudentNumber = studentNumber == null ? "" : studentNumber.trim();
            String claimantIdNumber = storedStudentNumber.isBlank()
                    ? generatedClaimantId(contactInfo)
                    : storedStudentNumber;
            int claimantId = DatabaseBootstrap.ensureUser(conn, claimantIdNumber, claimantName, contactInfo);
            int claimId;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, item.getId());
                stmt.setInt(2, claimantId);
                stmt.setString(3, status);
                stmt.setString(4, claimantName);
                stmt.setString(5, storedStudentNumber);
                stmt.setString(6, contactInfo);
                stmt.setString(7, proofDescription);
                stmt.setString(8, newTicket);
                
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw new IllegalStateException("Claim request was not saved.");
                }
                claimId = rs.getInt("claim_id");
            }
            return new ClaimRequest(claimId, item, claimantName, storedStudentNumber, contactInfo, proofDescription, status, newTicket);
        } catch (Exception e) {
            throw new IllegalStateException("Could not save claim request.", e);
        }
    }

    public void updateStatus(ClaimRequest request, String status) {
        DatabaseBootstrap.ensureApplicationSchema();
        String sql = "UPDATE claims SET claim_status = ? WHERE claim_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, request.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not update claim status.", e);
        }
    }

    public void updateDetails(ClaimRequest request, String newContact, String newProof) {
        DatabaseBootstrap.ensureApplicationSchema();
        String sql = "UPDATE claims SET contact_info = ?, proof_description = ? WHERE claim_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newContact);
            stmt.setString(2, newProof);
            stmt.setInt(3, request.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not update claim details.", e);
        }
    }

    public void delete(ClaimRequest request) {
        DatabaseBootstrap.ensureApplicationSchema();
        String sql = "DELETE FROM claims WHERE claim_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not delete claim request.", e);
        }
    }

    public void archiveClaim(ClaimRequest claim) {
        String sql = "UPDATE claims SET record_status = 'Archived' WHERE claim_id = ?";
        try (java.sql.Connection conn = DBConnection.connect();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, claim.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Could not archive claim: " + e.getMessage());
        }
    }

    public void restoreClaim(ClaimRequest claim) {
        String sql = "UPDATE claims SET record_status = 'Active' WHERE claim_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, claim.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not restore claim request.", e);
        }
    }

    public List<ClaimRequest> getArchivedClaims() {
        List<ClaimRequest> archivedList = new java.util.ArrayList<>();
        
        // FIX 2: Replicated the exact JOIN structure from findAll() so mapClaim() doesn't crash!
        String sql = """
                SELECT cl.claim_id, cl.claim_status, cl.claimant_name, cl.student_number,
                       cl.contact_info, cl.proof_description, cl.tracking_id,
                       claimant.full_name AS claimant_user_name, claimant.id_number AS claimant_id_number,
                       claimant.contact_number AS claimant_contact,
                       i.item_id, i.item_type, i.item_name, i.description, i.date_lost, i.date_found,
                       i.location, i.image_path, i.tracking_id AS item_tracking_id, c.category_name,
                       reporter.full_name AS reporter_name, reporter.contact_number AS reporter_contact
                FROM claims cl
                JOIN items i ON i.item_id = cl.item_id
                JOIN categories c ON c.category_id = i.category_id
                JOIN users reporter ON reporter.user_id = i.reporter_id
                JOIN users claimant ON claimant.user_id = cl.claimant_id
                WHERE cl.record_status = 'Archived'
                ORDER BY cl.claim_date DESC NULLS LAST, cl.claim_id DESC
                """;
        
        try (java.sql.Connection conn = DBConnection.connect();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                archivedList.add(mapClaim(rs));
            }
        } catch (Exception e) {
            System.err.println("Error fetching archived claims: " + e.getMessage());
        }
        return archivedList;
    }

    // --- MAPPER ---

    private ClaimRequest mapClaim(ResultSet rs) throws Exception {
        Date dateLost = rs.getDate("date_lost");
        Date dateFound = rs.getDate("date_found");
        String date = dateLost != null ? dateLost.toString() : dateFound != null ? dateFound.toString() : "";
        String itemName = rs.getString("item_name");
        if (itemName == null || itemName.isBlank()) {
            itemName = rs.getString("description");
        }

        ItemReport item = new ItemReport(
                rs.getInt("item_id"),
                rs.getString("item_type"),
                itemName,
                rs.getString("category_name"),
                date,
                rs.getString("location"),
                rs.getString("reporter_name"),
                rs.getString("reporter_contact"),
                rs.getString("description"),
                rs.getString("image_path"),
                rs.getString("item_tracking_id")
        );

        String claimantName = firstPresent(rs.getString("claimant_name"), rs.getString("claimant_user_name"));
        String studentNumber = firstPresent(rs.getString("student_number"), rs.getString("claimant_id_number"));
        String contactInfo = firstPresent(rs.getString("contact_info"), rs.getString("claimant_contact"));

        return new ClaimRequest(
                rs.getInt("claim_id"),
                item,
                claimantName,
                studentNumber,
                contactInfo,
                rs.getString("proof_description"),
                rs.getString("claim_status"),
                rs.getString("tracking_id") 
        );
    }

    private String firstPresent(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String generatedClaimantId(String contact) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = String.valueOf(Math.abs(contact.hashCode()));
        if (suffix.length() > 4) {
            suffix = suffix.substring(0, 4);
        }
        return "CLM" + timestamp.substring(Math.max(0, timestamp.length() - 13)) + suffix;
    }
}
