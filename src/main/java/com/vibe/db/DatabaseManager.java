package com.vibe.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vibe.model.Track;

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

            // User Queue
            stmt.execute("CREATE TABLE IF NOT EXISTS user_queue (" +
                    "user_id TEXT, " + // Reserved for future multi-user validaton
                    "track_id TEXT, " +
                    "position INTEGER, " +
                    "PRIMARY KEY (user_id, position), " +
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

    public static boolean deletePlaylist(String playlistId) {
        String sql1 = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        String sql2 = "DELETE FROM playlists WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                pstmt.setString(1, playlistId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setString(1, playlistId);
                int affected = pstmt2.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeTrackFromPlaylist(String playlistId, String trackId) {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND track_id = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playlistId);
            pstmt.setString(2, trackId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteTrack(String trackId) {
        // First remove from all playlists
        String sql1 = "DELETE FROM playlist_songs WHERE track_id = ?";
        String sql2 = "DELETE FROM tracks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                pstmt.setString(1, trackId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setString(1, trackId);
                int affected = pstmt2.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Queue Persistence ---

    public static void saveQueue(List<Track> queue) {
        // We act as a single user for now, using a fixed ID or just ignoring user_id for select *
        String userId = "default_user";
        
        String clearSql = "DELETE FROM user_queue WHERE user_id = ?";
        String insertSql = "INSERT INTO user_queue(user_id, track_id, position) VALUES(?,?,?)";
        
        try (Connection conn = DriverManager.getConnection(OB_URL)) {
            conn.setAutoCommit(false); // Transaction
            
            try (PreparedStatement clearStmt = conn.prepareStatement(clearSql)) {
                clearStmt.setString(1, userId);
                clearStmt.executeUpdate();
            }
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                int pos = 0;
                for (Track t : queue) {
                    insertStmt.setString(1, userId);
                    insertStmt.setString(2, t.getId());
                    insertStmt.setInt(3, pos++);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
            
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Track> loadQueue() {
        List<Track> list = new ArrayList<>();
        String userId = "default_user";
        String sql = "SELECT t.* FROM tracks t " +
                     "JOIN user_queue uq ON t.id = uq.track_id " +
                     "WHERE uq.user_id = ? " +
                     "ORDER BY uq.position ASC";
                     
        try (Connection conn = DriverManager.getConnection(OB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try(ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Track(
                        rs.getString("id"),
                        rs.getString("filepath"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("album"),
                        rs.getLong("duration")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- Settings Persistence ---

    public static String getSetting(String key) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (Connection conn = DriverManager.getConnection(OB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setSetting(String key, String value) {
        String sql = "INSERT OR REPLACE INTO settings(key, value) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(OB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
