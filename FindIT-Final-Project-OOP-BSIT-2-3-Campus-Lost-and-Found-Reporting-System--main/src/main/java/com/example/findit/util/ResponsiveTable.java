package com.example.findit.util;

import javafx.scene.control.TableView;

public final class ResponsiveTable {
    private ResponsiveTable() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void fillAvailableWidth(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
