package com.vibe;

import java.io.File;

import com.vibe.model.Track;
import com.vibe.structures.DoublyLinkedList;
import com.vibe.structures.HistoryStack;
import com.vibe.structures.PlayQueue;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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
        playTrack(track, true);
    }

    // Overload: allow skipping adding the current track to history when set to false
    public void playTrack(Track track, boolean pushToHistory) {
        // Stop previous
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        // Optionally add to history
        if (pushToHistory && currentTrack.get() != null) {
            history.push(currentTrack.get());
        }

        // Load new
        try {
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
            // Try to find and sync currentNode in playlist to keep the linked-list context consistent
            DoublyLinkedList.Node<Track> iter = playlist.getHead();
            while (iter != null) {
                if (iter.value.getId().equals(prev.getId())) {
                    currentNode = iter;
                    break;
                }
                iter = iter.next;
            }
            // Play without pushing current track back into history
            playTrack(prev, false);
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

    /** Pause playback (keeps current position). */
    public void pause() {
        if (mediaPlayer != null && isPlaying.get()) {
            mediaPlayer.pause();
            isPlaying.set(false);
        }
    }

    /** Resume playback from current position. */
    public void play() {
        if (mediaPlayer != null && !isPlaying.get()) {
            mediaPlayer.play();
            isPlaying.set(true);
        }
    }

    /** Seek to a specific time (in seconds) within the current track. */
    public void seek(double seconds) {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.seek(javafx.util.Duration.seconds(seconds));
            // Update current time property immediately so UI reflects position
            currentTime.set(seconds);
        } catch (Exception e) {
            System.err.println("Seek failed: " + e.getMessage());
        }
    }
}
