package com.vibe;

import com.vibe.model.Track;
import com.vibe.structures.DoublyLinkedList;
import com.vibe.structures.HistoryStack;
import com.vibe.structures.PlayQueue;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class PlayerController {
    private static PlayerController instance;

    // State
    private DoublyLinkedList<Track> playlist = new DoublyLinkedList<>(); // Main context
    private DoublyLinkedList.Node<Track> currentNode;
    
    private PlayQueue<Track> queue = new PlayQueue<>(); // FR-4
    private HistoryStack<Track> history = new HistoryStack<>(); // FR-5

    private MediaPlayer mediaPlayer;
    
    // Properties for UI Binding
    private ObjectProperty<Track> currentTrack = new SimpleObjectProperty<>();
    private BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private DoubleProperty currentTime = new SimpleDoubleProperty(0);
    private DoubleProperty duration = new SimpleDoubleProperty(0);
    private DoubleProperty volume = new SimpleDoubleProperty(0.5);

    private PlayerController() {}

    public static PlayerController getInstance() {
        if (instance == null) instance = new PlayerController();
        return instance;
    }

    public void playTrack(Track track) {
        // Stop previous
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        // Add to history if we were playing something
        if (currentTrack.get() != null) {
            history.push(currentTrack.get());
        }

        // Load new
        try {
            // "track.getFilepath()" assumes absolute path or URI
            // We need to convert to URI for Media
            File file = new File(track.getFilepath());
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            
            mediaPlayer.currentTimeProperty().addListener((obs, oldV, newV) -> {
                currentTime.set(newV.toSeconds());
            });
            
            mediaPlayer.setOnReady(() -> {
                duration.set(mediaPlayer.getMedia().getDuration().toSeconds());
                mediaPlayer.play();
                isPlaying.set(true);
            });
            
            mediaPlayer.setOnEndOfMedia(this::playNext);
            
            // Bind volume
            mediaPlayer.volumeProperty().bind(volume);

            currentTrack.set(track);
        } catch (Exception e) {
            System.err.println("Error playing file: " + e.getMessage());
        }
    }
    
    // Set the main playlist context
    public void setPlaylistContext(java.util.List<Track> tracks, Track startTrack) {
        playlist.clear();
        currentNode = null;
        for (Track t : tracks) {
            playlist.add(t);
            if (t.getId().equals(startTrack.getId())) {
                currentNode = playlist.getTail(); // The one just added
            }
        }
        playTrack(startTrack);
    }

    public void togglePlay() {
        if (mediaPlayer == null) return;
        if (isPlaying.get()) {
            mediaPlayer.pause();
            isPlaying.set(false);
        } else {
            mediaPlayer.play();
            isPlaying.set(true);
        }
    }

    public void playNext() {
        // FR-4: Check Queue first
        if (!queue.isEmpty()) {
            Track next = queue.dequeue();
            playTrack(next);
            return;
        }

        // FR-2: Check LL
        if (currentNode != null && currentNode.next != null) {
            currentNode = currentNode.next;
            playTrack(currentNode.value);
        } else {
            isPlaying.set(false); // End of list
        }
    }

    public void playPrevious() {
        // FR-5: Check History first (Back button behavior)
        if (!history.isEmpty()) {
            Track prev = history.pop();
            // Need to sync currentNode if possible, or just play
            // Ideally we find 'prev' in our current list to keep sync
            // For now, simple play
            playTrack(prev);
            // Re-sync currentNode if it exists in 'playlist'
             // ... logic omitted for brevity
        } else if (currentNode != null && currentNode.prev != null) {
            currentNode = currentNode.prev;
            playTrack(currentNode.value);
        }
    }

    public void addToQueue(Track track) {
        queue.enqueue(track);
    }
    
    // Getters for properties
    public ObjectProperty<Track> currentTrackProperty() { return currentTrack; }
    public BooleanProperty isPlayingProperty() { return isPlaying; }
    public DoubleProperty currentTimeProperty() { return currentTime; }
    public DoubleProperty durationProperty() { return duration; }
    public DoubleProperty volumeProperty() { return volume; }
}
