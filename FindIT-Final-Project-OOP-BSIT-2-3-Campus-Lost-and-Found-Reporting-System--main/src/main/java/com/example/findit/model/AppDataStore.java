package com.example.findit.model;

import com.example.findit.dao.ClaimRequestDAO;
import com.example.findit.dao.ItemReportDAO;
import com.example.findit.dao.ValidationDAO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public final class AppDataStore {
    private static final long REALTIME_REFRESH_SECONDS = 2;
    private static final Set<String> MATCH_STOP_WORDS = Set.of(
            "a", "an", "and", "at", "for", "from", "in", "is", "it", "lost", "found",
            "my", "near", "of", "on", "or", "the", "this", "to", "with"
    );
    private static final ObservableList<ItemReport> ITEM_REPORTS = FXCollections.observableArrayList();
    private static final ObservableList<ClaimRequest> CLAIM_REQUESTS = FXCollections.observableArrayList();
    private static final ObservableList<ItemMatch> MATCH_SUGGESTIONS = FXCollections.observableArrayList();
    private static final Set<Integer> DECLINED_MATCH_IDS = new HashSet<>();
    private static final ItemReportDAO ITEM_REPORT_DAO = new ItemReportDAO();
    private static final ClaimRequestDAO CLAIM_REQUEST_DAO = new ClaimRequestDAO();
    private static final ValidationDAO VALIDATION_DAO = new ValidationDAO();
    public static final ObservableList<ItemReport> ARCHIVED_ITEMS = FXCollections.observableArrayList();
    public static final javafx.collections.ObservableList<ClaimRequest> ARCHIVED_CLAIMS = javafx.collections.FXCollections.observableArrayList();
    private static final ScheduledExecutorService REALTIME_REFRESHER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "findit-realtime-refresh");
        thread.setDaemon(true);
        return thread;
    });
    private static final AtomicLong LOCAL_CHANGE_VERSION = new AtomicLong();
    private static boolean realtimeUpdatesStarted;

    static {
        refreshAll();
        startRealtimeUpdates();
        disposeUnclaimedItems();
        refreshArchivedItems();
    }

    private AppDataStore() {
    }

    public static ObservableList<ItemReport> getItemReports() {
        return ITEM_REPORTS;
    }

    public static ObservableList<ClaimRequest> getClaimRequests() {
        return CLAIM_REQUESTS;
    }

    public static ObservableList<ItemMatch> getMatchSuggestions() {
        return MATCH_SUGGESTIONS;
    }

    public static ItemReport addItemReport(String type, String itemName, String category, String date,
                                           String location, String reportedBy, String contact,
                                           String description, String imagePath) {
        ItemReport report = ITEM_REPORT_DAO.insert(type, itemName, category, date, location,
                reportedBy, contact, description, imagePath);
        ITEM_REPORTS.add(0, report);
        markLocalChange();
        refreshMatchSuggestions();
        return report;
    }

    public static ClaimRequest addClaimRequest(ItemReport item, String claimantName, String studentNumber,
                                               String contactInfo, String proofDescription) {
        if (item == null || !"Found".equalsIgnoreCase(item.getType())) {
            throw new IllegalArgumentException("Only found items can be claimed.");
        }

        ClaimRequest request = CLAIM_REQUEST_DAO.insert(item, claimantName, studentNumber,
                contactInfo, proofDescription);
        CLAIM_REQUESTS.add(0, request);
        markLocalChange();
        refreshMatchSuggestions();
        return request;
    }

    public static ClaimRequest confirmMatch(ItemMatch match) {
        ClaimRequest existing = findAutoMatchClaim(match);
        if (existing != null) {
            if (!"Approved".equalsIgnoreCase(existing.getStatus())) {
                CLAIM_REQUEST_DAO.updateStatus(existing, "Approved");
                existing.setStatus("Approved");
                logClaimValidation(existing, "Approved");
                markLocalChange();
            }
            match.setStatus("Confirmed");
            refreshMatchSuggestions();
            return existing;
        }

        ItemReport lostItem = match.getLostItem();
        ItemReport foundItem = match.getFoundItem();
        String proof = "Auto-generated from match suggestion between lost report #"
                + lostItem.getId() + " and found report #" + foundItem.getId() + ".";

        ClaimRequest request = CLAIM_REQUEST_DAO.insert(
                foundItem,
                lostItem.getReportedBy(),
                autoMatchStudentNumber(match),
                lostItem.getContact(),
                proof
        );
        CLAIM_REQUEST_DAO.updateStatus(request, "Approved");
        request.setStatus("Approved");
        logClaimValidation(request, "Approved");
        CLAIM_REQUESTS.add(0, request);
        markLocalChange();
        match.setStatus("Confirmed");
        refreshMatchSuggestions();
        return request;
    }

    public static ClaimRequest rejectMatch(ItemMatch match) {
        if (match == null) {
            return null;
        }

        ClaimRequest existing = findAutoMatchClaim(match);
        if (existing != null) {
            if (!"Rejected".equalsIgnoreCase(existing.getStatus())) {
                CLAIM_REQUEST_DAO.updateStatus(existing, "Rejected");
                existing.setStatus("Rejected");
                logClaimValidation(existing, "Rejected");
                markLocalChange();
            }
            declineMatch(match);
            return existing;
        }

        ItemReport lostItem = match.getLostItem();
        ItemReport foundItem = match.getFoundItem();
        String proof = "Auto-generated rejected match suggestion between lost report #"
                + lostItem.getId() + " and found report #" + foundItem.getId() + ".";

        ClaimRequest request = CLAIM_REQUEST_DAO.insert(
                foundItem,
                lostItem.getReportedBy(),
                autoMatchStudentNumber(match),
                lostItem.getContact(),
                proof,
                "Rejected"
        );
        logClaimValidation(request, "Rejected");
        CLAIM_REQUESTS.add(0, request);
        markLocalChange();
        declineMatch(match);
        return request;
    }

    public static void deleteItemReport(ItemReport item) {
        ITEM_REPORT_DAO.delete(item);
        ITEM_REPORTS.remove(item);
        CLAIM_REQUESTS.removeIf(claim -> claim.getItem().getId() == item.getId());
        markLocalChange();
        refreshMatchSuggestions();
    }

    public static void updateClaimStatus(ClaimRequest request, String status) {
        CLAIM_REQUEST_DAO.updateStatus(request, status);
        request.setStatus(status);
        logClaimValidation(request, status);
        markLocalChange();
        refreshMatchSuggestions();
    }

    public static void deleteClaimRequest(ClaimRequest request) {
        CLAIM_REQUEST_DAO.delete(request);
        CLAIM_REQUESTS.remove(request);
        markLocalChange();
        refreshMatchSuggestions();
    }

    public static void refreshAll() {
        try {
            ITEM_REPORTS.setAll(ITEM_REPORT_DAO.findAll());
            CLAIM_REQUESTS.setAll(CLAIM_REQUEST_DAO.findAll());
            refreshMatchSuggestions();
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    public static synchronized void startRealtimeUpdates() {
        if (realtimeUpdatesStarted || REALTIME_REFRESHER.isShutdown()) {
            return;
        }

        realtimeUpdatesStarted = true;
        REALTIME_REFRESHER.scheduleWithFixedDelay(
                AppDataStore::refreshFromDatabaseInBackground,
                REALTIME_REFRESH_SECONDS,
                REALTIME_REFRESH_SECONDS,
                TimeUnit.SECONDS
        );
    }

    public static void stopRealtimeUpdates() {
        REALTIME_REFRESHER.shutdownNow();
    }

    public static long countItemsByType(String type) {
        return ITEM_REPORTS.stream()
                .filter(item -> item.getType().equalsIgnoreCase(type))
                .count();
    }

    public static long countClaimsByStatus(String status) {
        return CLAIM_REQUESTS.stream()
                .filter(claim -> claim.getStatus().equalsIgnoreCase(status))
                .count();
    }

    public static long countMatches() {
        return MATCH_SUGGESTIONS.size();
    }

    private static void refreshMatchSuggestions() {
        ObservableList<ItemMatch> matches = FXCollections.observableArrayList();
        for (ItemReport lostItem : ITEM_REPORTS) {
            if (!"Lost".equalsIgnoreCase(lostItem.getType())) {
                continue;
            }
            for (ItemReport foundItem : ITEM_REPORTS) {
                if (!"Found".equalsIgnoreCase(foundItem.getType())) {
                    continue;
                }
                if (isPotentialMatch(lostItem, foundItem)) {
                    int matchId = matchId(lostItem, foundItem);
                    if (DECLINED_MATCH_IDS.contains(matchId)) {
                        continue;
                    }

                    ItemMatch match = new ItemMatch(
                            matchId,
                            lostItem,
                            foundItem,
                            "Pending"
                    );
                    ClaimRequest autoMatchClaim = findAutoMatchClaim(match);
                    ClaimRequest relatedClaim = autoMatchClaim != null ? autoMatchClaim : findLatestClaimForItem(foundItem);
                    if (autoMatchClaim != null && "Rejected".equalsIgnoreCase(autoMatchClaim.getStatus())) {
                        continue;
                    }
                    if (relatedClaim != null && "Approved".equalsIgnoreCase(relatedClaim.getStatus())) {
                        match.setStatus("Confirmed");
                    }
                    matches.add(match);
                }
            }
        }
        MATCH_SUGGESTIONS.setAll(matches);
    }

    public static void declineMatch(ItemMatch match) {
        if (match == null) {
            return;
        }

        DECLINED_MATCH_IDS.add(match.getId());
        MATCH_SUGGESTIONS.removeIf(existingMatch -> existingMatch.getId() == match.getId());
    }

    private static boolean isPotentialMatch(ItemReport lostItem, ItemReport foundItem) {
        Set<String> lostNameKeywords = keywordsFrom(lostItem.getItemName());
        Set<String> foundNameKeywords = keywordsFrom(foundItem.getItemName());
        int matchingNameKeywords = countMatchingKeywords(lostNameKeywords, foundNameKeywords);

        return matchingNameKeywords >= 2 || hasStrongMatchingKeyword(lostNameKeywords, foundNameKeywords);
    }

    private static Set<String> keywordsFrom(String value) {
        Set<String> keywords = new HashSet<>();
        if (value == null || value.isBlank()) {
            return keywords;
        }

        Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .map(String::trim)
                .filter(word -> word.length() >= 2)
                .filter(word -> !MATCH_STOP_WORDS.contains(word))
                .forEach(keywords::add);
        return keywords;
    }

    private static int countMatchingKeywords(Set<String> lostKeywords, Set<String> foundKeywords) {
        int count = 0;
        for (String lostKeyword : lostKeywords) {
            for (String foundKeyword : foundKeywords) {
                if (areSimilarKeywords(lostKeyword, foundKeyword)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private static boolean hasStrongMatchingKeyword(Set<String> lostKeywords, Set<String> foundKeywords) {
        for (String lostKeyword : lostKeywords) {
            for (String foundKeyword : foundKeywords) {
                if (Math.max(lostKeyword.length(), foundKeyword.length()) >= 4
                        && areSimilarKeywords(lostKeyword, foundKeyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean areSimilarKeywords(String first, String second) {
        return first.equals(second)
                || first.contains(second)
                || second.contains(first);
    }

    private static String normalized(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static int matchId(ItemReport lostItem, ItemReport foundItem) {
        return Math.abs((lostItem.getId() + ":" + foundItem.getId()).hashCode());
    }

    private static ClaimRequest findAutoMatchClaim(ItemMatch match) {
        String autoStudentNumber = autoMatchStudentNumber(match);
        return CLAIM_REQUESTS.stream()
                .filter(claim -> claim.getItem().getId() == match.getFoundItem().getId())
                .filter(claim -> autoStudentNumber.equals(claim.getStudentNumber()))
                .findFirst()
                .orElse(null);
    }

    private static ClaimRequest findLatestClaimForItem(ItemReport item) {
        return CLAIM_REQUESTS.stream()
                .filter(claim -> claim.getItem().getId() == item.getId())
                .findFirst()
                .orElse(null);
    }

    private static String autoMatchStudentNumber(ItemMatch match) {
        return "MATCH-" + match.getLostItem().getId() + "-" + match.getFoundItem().getId();
    }

    public static void updateItemDetails(ItemReport item, String newName, String newLocation, String newDescription) {
        ITEM_REPORT_DAO.updateDetails(item, newName, newLocation, newDescription);
        markLocalChange();
        refreshAll(); 
    }

    public static void updateClaimDetails(ClaimRequest request, String newContact, String newProof) {
        CLAIM_REQUEST_DAO.updateDetails(request, newContact, newProof);
        markLocalChange();
        refreshAll();
    }

    private static void refreshFromDatabaseInBackground() {
        long refreshVersion = LOCAL_CHANGE_VERSION.get();
        try {
            List<ItemReport> latestItems = ITEM_REPORT_DAO.findAll();
            List<ClaimRequest> latestClaims = CLAIM_REQUEST_DAO.findAll();

            Platform.runLater(() -> {
                if (refreshVersion != LOCAL_CHANGE_VERSION.get()) {
                    return;
                }

                boolean changed = syncItemReports(latestItems) | syncClaimRequests(latestClaims);
                if (changed) {
                    refreshMatchSuggestions();
                }
            });
        } catch (IllegalStateException e) {
            if (e.getMessage() == null || !e.getMessage().contains("Toolkit not initialized")) {
                System.err.println(e.getMessage());
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    private static boolean syncItemReports(List<ItemReport> latestItems) {
        if (sameItemReports(ITEM_REPORTS, latestItems)) {
            return false;
        }

        ITEM_REPORTS.setAll(latestItems);
        return true;
    }

    private static boolean syncClaimRequests(List<ClaimRequest> latestClaims) {
        if (sameClaimRequests(CLAIM_REQUESTS, latestClaims)) {
            return false;
        }

        CLAIM_REQUESTS.setAll(latestClaims);
        return true;
    }

    private static boolean sameItemReports(List<ItemReport> current, List<ItemReport> latest) {
        if (current.size() != latest.size()) {
            return false;
        }

        for (int i = 0; i < current.size(); i++) {
            if (!sameItemReport(current.get(i), latest.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameClaimRequests(List<ClaimRequest> current, List<ClaimRequest> latest) {
        if (current.size() != latest.size()) {
            return false;
        }

        for (int i = 0; i < current.size(); i++) {
            ClaimRequest currentClaim = current.get(i);
            ClaimRequest latestClaim = latest.get(i);
            if (currentClaim.getId() != latestClaim.getId()
                    || !sameItemReport(currentClaim.getItem(), latestClaim.getItem())
                    || !same(currentClaim.getClaimantName(), latestClaim.getClaimantName())
                    || !same(currentClaim.getStudentNumber(), latestClaim.getStudentNumber())
                    || !same(currentClaim.getContactInfo(), latestClaim.getContactInfo())
                    || !same(currentClaim.getProofDescription(), latestClaim.getProofDescription())
                    || !same(currentClaim.getStatus(), latestClaim.getStatus())
                    || !same(currentClaim.getTrackingId(), latestClaim.getTrackingId())) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameItemReport(ItemReport currentItem, ItemReport latestItem) {
        return currentItem.getId() == latestItem.getId()
                && same(currentItem.getType(), latestItem.getType())
                && same(currentItem.getItemName(), latestItem.getItemName())
                && same(currentItem.getCategory(), latestItem.getCategory())
                && same(currentItem.getDate(), latestItem.getDate())
                && same(currentItem.getLocation(), latestItem.getLocation())
                && same(currentItem.getReportedBy(), latestItem.getReportedBy())
                && same(currentItem.getContact(), latestItem.getContact())
                && same(currentItem.getDescription(), latestItem.getDescription())
                && same(currentItem.getImagePath(), latestItem.getImagePath())
                && same(currentItem.getTrackingId(), latestItem.getTrackingId());
    }

    private static boolean same(String current, String latest) {
        if (current == null) {
            return latest == null;
        }
        return current.equals(latest);
    }

    private static void markLocalChange() {
        LOCAL_CHANGE_VERSION.incrementAndGet();
    }

    private static void logClaimValidation(ClaimRequest request, String status) {
        User admin = SessionManager.getCurrentUser();
        if (request == null || request.getItem() == null || admin == null || !SessionManager.isCurrentUserAdmin()) {
            return;
        }

        String validationType = normalizeValidationType(status);
        String remarks = buildClaimValidationRemarks(request, status, admin);

        try {
            VALIDATION_DAO.logValidation(
                    request.getItem().getId(),
                    admin.getUserId(),
                    validationType,
                    remarks
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static String normalizeValidationType(String status) {
        if ("Approved".equalsIgnoreCase(status)) {
            return "APPROVED";
        }
        if ("Rejected".equalsIgnoreCase(status)) {
            return "REJECTED";
        }
        return "PENDING";
    }
    //archive logic
    public static void archiveItemReport(ItemReport item) {
        ITEM_REPORT_DAO.archiveItem(item);
        ITEM_REPORTS.remove(item);  
        if (!ARCHIVED_ITEMS.contains(item)) {
            ARCHIVED_ITEMS.add(item);          
        }
        CLAIM_REQUESTS.removeIf(claim -> claim.getItem().getId() == item.getId());
        refreshMatchSuggestions();
    }

    public static void archiveClaimRequest(ClaimRequest claim) {
        CLAIM_REQUEST_DAO.archiveClaim(claim); 
        CLAIM_REQUESTS.remove(claim);          
        
        if (!ARCHIVED_CLAIMS.contains(claim)) {
            ARCHIVED_CLAIMS.add(claim);        
        }
        refreshMatchSuggestions();
    }

    public static int disposeUnclaimedItems() {
        int archivedCount = 0;
        LocalDate today = LocalDate.now();
        List<ItemReport> itemsToDispose = new ArrayList<>();

        for (ItemReport item : ITEM_REPORTS) {
            if ("Found".equalsIgnoreCase(item.getType())) {
                try {
                    LocalDate itemDate = LocalDate.parse(item.getDate());
                    long daysOld = ChronoUnit.DAYS.between(itemDate, today);

                    if (daysOld > 60) {
                        boolean isClaimed = CLAIM_REQUESTS.stream()
                                .anyMatch(claim -> claim.getItem().getId() == item.getId() 
                                                && "Approved".equalsIgnoreCase(claim.getStatus()));

                        if (!isClaimed) {
                            itemsToDispose.add(item);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Date parse error for item: " + item.getId());
                }
            }
        }

        for (ItemReport item : itemsToDispose) {
            archiveItemReport(item); 
            archivedCount++;
        }
        return archivedCount;
    }

    public static void refreshArchivedItems() {
        ARCHIVED_ITEMS.setAll(ITEM_REPORT_DAO.getArchivedItems());
    }

    private static String buildClaimValidationRemarks(ClaimRequest request, String status, User admin) {
        String remarks = "Admin " + safe(admin.getFullName())
                + " set claim #" + request.getId()
                + " for item \"" + safe(request.getItem().getItemName()) + "\""
                + " to " + safe(status)
                + ". Claimant: " + safe(request.getClaimantName())
                + " (" + safe(request.getStudentNumber()) + ").";

        return remarks.length() > 500 ? remarks.substring(0, 500) : remarks;
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }
}
