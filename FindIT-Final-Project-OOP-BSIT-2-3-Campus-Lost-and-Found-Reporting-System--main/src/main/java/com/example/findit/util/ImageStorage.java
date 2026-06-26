package com.example.findit.util;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;

public final class ImageStorage {
    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final String DATA_IMAGE_PREFIX = "data:image/";

    private ImageStorage() {
    }

    public static String toPortableImagePath(File file) throws IOException {
        if (file == null) {
            return null;
        }

        long fileSize = Files.size(file.toPath());
        if (fileSize > MAX_IMAGE_BYTES) {
            throw new IOException("Image is larger than 5 MB.");
        }

        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null || !mimeType.startsWith("image/")) {
            mimeType = mimeTypeFromName(file.getName());
        }

        byte[] imageBytes = Files.readAllBytes(file.toPath());
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    public static Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        try {
            if (imagePath.startsWith(DATA_IMAGE_PREFIX)) {
                int dataStart = imagePath.indexOf(',');
                if (dataStart < 0 || dataStart == imagePath.length() - 1) {
                    return null;
                }

                byte[] imageBytes = Base64.getDecoder().decode(imagePath.substring(dataStart + 1));
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                return image.isError() ? null : image;
            }

            String source = normalizeImageSource(imagePath);
            Image image = new Image(source, false);
            return image.isError() ? null : image;
        } catch (RuntimeException e) {
            System.err.println("Could not load item image: " + e.getMessage());
            return null;
        }
    }

    private static String normalizeImageSource(String imagePath) {
        String trimmedPath = imagePath.trim();
        File directFile = new File(trimmedPath);
        if (directFile.exists()) {
            return directFile.toURI().toString();
        }

        if (trimmedPath.startsWith("file:")) {
            try {
                File uriFile = new File(new URI(trimmedPath));
                if (uriFile.exists()) {
                    return uriFile.toURI().toString();
                }
            } catch (IllegalArgumentException | URISyntaxException e) {
                return trimmedPath;
            }
        }

        return trimmedPath;
    }

    private static String mimeTypeFromName(String fileName) {
        String lowerName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".png")) {
            return "image/png";
        }
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "image/png";
    }
}
