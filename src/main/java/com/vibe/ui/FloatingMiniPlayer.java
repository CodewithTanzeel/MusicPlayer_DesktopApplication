package com.vibe.ui;

import com.vibe.PlayerController;
import com.vibe.model.Track;
import com.vibe.service.CoverArtService;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FloatingMiniPlayer extends Stage {

    private PlayerController player = PlayerController.getInstance();
    private ImageView coverView;
    private Label trackTitle;
    private Label trackArtist;
    private Button playBtn;
    private Runnable onRestoreAction;

    // Offsets for dragging
    private double xOffset = 0;
    private double yOffset = 0;

    public FloatingMiniPlayer(Runnable onRestoreAction) {
        this.onRestoreAction = onRestoreAction;
        
        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);
        setTitle("Mini Player");

        // Root container (rounded and semi-transparent)
        HBox root = new HBox(10);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: rgba(24, 24, 27, 0.9); -fx-background-radius: 12; -fx-border-color: #3f3f46; -fx-border-radius: 12; -fx-padding: 10;");
        root.setPrefWidth(300);
        root.setPrefHeight(80);

        // --- Dragging Logic ---
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });

        // --- UI Components ---
        coverView = new ImageView();
        coverView.setFitWidth(50);
        coverView.setFitHeight(50);
        coverView.setPreserveRatio(true);
        coverView.setSmooth(true);
        // Rounded clip? For now just square image
        
        VBox info = new VBox(2);
        info.setAlignment(Pos.CENTER_LEFT);
        trackTitle = new Label("-");
        trackTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        trackArtist = new Label("-");
        trackArtist.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 10px;");
        info.getChildren().addAll(trackTitle, trackArtist);
        
        // Controls
        playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
        playBtn.setOnAction(e -> player.togglePlay());
        
        Button nextBtn = new Button(">>");
        nextBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        nextBtn.setOnAction(e -> player.playNext());

        Button restoreBtn = new Button("↙"); // Restore icon
        restoreBtn.setTooltip(new javafx.scene.control.Tooltip("Restore to Main Player"));
        restoreBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        restoreBtn.setOnAction(e -> restore());
        
        Button closeBtn = new Button("x");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> restore()); // Closing mini player should probably restore main player or just exit? Let's restore.

        // Layout
        HBox controls = new HBox(5);
        controls.setAlignment(Pos.CENTER_RIGHT);
        controls.getChildren().addAll(playBtn, nextBtn, restoreBtn);
        
        root.getChildren().addAll(coverView, info, controls);
        HBox.setHgrow(info, Priority.ALWAYS);
        HBox.setHgrow(controls, Priority.NEVER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Initial Data
        updateTrack(player.currentTrackProperty().get());
        updatePlayState(player.isPlayingProperty().get());

        // Listeners
        player.currentTrackProperty().addListener((obs, old, track) -> updateTrack(track));
        player.isPlayingProperty().addListener((obs, old, playing) -> updatePlayState(playing));
    }

    private void updateTrack(Track track) {
        if (track == null) return;
        trackTitle.setText(track.getTitle());
        trackArtist.setText(track.getArtist());
        Image art = CoverArtService.getInstance().getCoverArt(track);
        coverView.setImage(art);
    }

    private void updatePlayState(boolean playing) {
        playBtn.setText(playing ? "⏸" : "▶");
    }

    private void restore() {
        if (onRestoreAction != null) onRestoreAction.run();
        close();
    }
}
