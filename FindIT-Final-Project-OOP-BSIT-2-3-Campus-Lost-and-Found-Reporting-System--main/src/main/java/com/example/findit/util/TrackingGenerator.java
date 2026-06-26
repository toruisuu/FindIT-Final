package com.example.findit.util;

import java.util.Random;

public class TrackingGenerator {
    
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new Random();

    public static String generateID() {
        StringBuilder id = new StringBuilder();
        
        // 2 Random Letters
        for (int i = 0; i < 2; i++) {
            id.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }
        
        id.append("-");
        
        // 4 Random Numbers
        for (int i = 0; i < 4; i++) {
            id.append(RANDOM.nextInt(10));
        }
        
        return id.toString();
    }
}