package com.example.findit.model;

public class ItemMatch {
    private final int id;
    private final ItemReport lostItem;
    private final ItemReport foundItem;
    private String status;

    public ItemMatch(int id, ItemReport lostItem, ItemReport foundItem, String status) {
        this.id = id;
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public ItemReport getLostItem() {
        return lostItem;
    }

    public ItemReport getFoundItem() {
        return foundItem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
