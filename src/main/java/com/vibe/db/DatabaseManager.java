package com.vibe.db;

import com.vibe.model.Track;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private static final String OB_URL = "jdbc:sqlite:vibe_music.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(OB_URL)) {
            Statement stmt = conn.createStatement();

            // Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY, " +
                    "username TEXT UNIQUE, " +
                    "password TEXT)");

            // Tracks
            stmt.execute("CREATE TABLE IF NOT EXISTS tracks (" +
                    "id TEXT PRIMARY KEY, " +
                    "filepath TEXT UNIQUE, " +
                    "title TEXT, " +
                    "artist TEXT, " +
                    "album TEXT, " +
                    "duration INTEGER)");

            // Playlists
            stmt.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT UNIQUE)");

            // Playlist Songs
            stmt.execute("CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "playlist_id TEXT, " +
                    "track_id TEXT, " +
                    "FOREIGN KEY(playlist_id) REFERENCES playlists(id), " +
                    "FOREIGN KEY(track_id) REFERENCES tracks(id))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(String username, String password) {
        if (checkUserExists(username)) {
            return false;
        }

        String sql = "INSERT INTO users(id, username, password) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, username);
            pstmt.setString(3, password); // In real app, hash this!
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkUserExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginUser(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addTrack(Track track) {
        String sql = "INSERT OR IGNORE INTO tracks(id, filepath, title, artist, album, duration) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, track.getId());
            pstmt.setString(2, track.getFilepath());
            pstmt.setString(3, track.getTitle());
            pstmt.setString(4, track.getArtist());
            pstmt.setString(5, track.getAlbum());
            pstmt.setLong(6, track.getDuration());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Track> getAllTracks() {
        List<Track> list = new ArrayList<>();
        String sql = "SELECT * FROM tracks";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Track(
                        rs.getString("id"),
                        rs.getString("filepath"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("album"),
                        rs.getLong("duration")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- Playlist DAO Methods ---

    public static boolean checkPlaylistExists(String name) {
        String sql = "SELECT 1 FROM playlists WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createPlaylist(com.vibe.model.Playlist playlist) {
        String sql = "INSERT INTO playlists(id, name) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playlist.getId());
            pstmt.setString(2, playlist.getName());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<com.vibe.model.Playlist> getAllPlaylists() {
        List<com.vibe.model.Playlist> list = new ArrayList<>();
        String sql = "SELECT * FROM playlists";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new com.vibe.model.Playlist(
                        rs.getString("id"),
                        rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void addTrackToPlaylist(String playlistId, String trackId) {
        String sql = "INSERT INTO playlist_songs(playlist_id, track_id) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playlistId);
            pstmt.setString(2, trackId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Track> getTracksForPlaylist(String playlistId) {
        List<Track> list = new ArrayList<>();
        String sql = "SELECT t.* FROM tracks t " +
                "JOIN playlist_songs ps ON t.id = ps.track_id " +
                "WHERE ps.playlist_id = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playlistId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Track(
                        rs.getString("id"),
                        rs.getString("filepath"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("album"),
                        rs.getLong("duration")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
