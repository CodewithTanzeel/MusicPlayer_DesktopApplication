package com.vibe.service;

import com.vibe.model.Track;
import javafx.scene.image.Image;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CoverArtService {

    private static CoverArtService instance;
    private final Map<String, Image> cache;
    private final Image defaultImage;

    private CoverArtService() {
        this.cache = new HashMap<>();
        // Load default image (placeholder)
        // Ensure you have a placeholder.png in src/main/resources/images/ or similar
        // For now, we can leave it null or create a simple placeholder programmatically if needed,
        // but ideally we load a resource.
        this.defaultImage = null; // TODO: Load a real default image
    }

    public static synchronized CoverArtService getInstance() {
        if (instance == null) {
            instance = new CoverArtService();
        }
        return instance;
    }

    public Image getCoverArt(Track track) {
        if (track == null) return defaultImage;

        String cacheKey = track.getFilepath();
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        Image art = loadFromEmbedded(track.getFilepath());
        if (art == null) {
            art = loadFromFolder(track.getFilepath());
        }

        if (art != null) {
            cache.put(cacheKey, art);
            return art;
        }

        return defaultImage;
    }

    private Image loadFromEmbedded(String filepath) {
        try {
            File file = new File(filepath);
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    return new Image(new ByteArrayInputStream(imageData));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load embedded art for: " + filepath + " - " + e.getMessage());
        }
        return null;
    }

    private Image loadFromFolder(String filepath) {
        try {
            File file = new File(filepath);
            File parentDir = file.getParentFile();
            if (parentDir != null && parentDir.isDirectory()) {
                // Common cover art filenames
                String[] coverNames = {"cover.jpg", "Cover.jpg", "folder.jpg", "Folder.jpg", "artwork.jpg", "album.jpg"};
                
                for (String name : coverNames) {
                    File coverFile = new File(parentDir, name);
                    if (coverFile.exists()) {
                        return new Image(coverFile.toURI().toString());
                    }
                }
                
                // Fallback: try to find any image in the folder if strict names fail? 
                // Maybe too aggressive. Sticking to specific names is safer.
            }
        } catch (Exception e) {
            System.err.println("Failed to load folder art for: " + filepath + " - " + e.getMessage());
        }
        return null;
    }
    
    public void clearCache() {
        cache.clear();
    }
}
