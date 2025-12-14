package com.vibe.model;

public class Track {
    private String id;
    private String filepath;
    private String title;
    private String artist;
    private String album;
    private long duration; // in seconds

    public Track(String id, String filepath, String title, String artist, String album, long duration) {
        this.id = id;
        this.filepath = filepath;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    public String getId() { return id; }
    public String getFilepath() { return filepath; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public long getDuration() { return duration; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
