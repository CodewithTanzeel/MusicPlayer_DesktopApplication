package com.vibe.model;

import java.util.UUID;

public class Playlist {
    private String id;
    private String name;

    public Playlist(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Playlist(String name) {
        this(UUID.randomUUID().toString(), name);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
