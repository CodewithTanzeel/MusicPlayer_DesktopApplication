package com.vibe.ui;

import com.vibe.PlayerController;
import com.vibe.service.CoverArtService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.Cursor;

public class MiniPlayerBar extends HBox {

    private PlayerController player = PlayerController.getInstance();
    private ImageView coverView;
    private Label trackTitle;
    private Label trackArtist;
    private Button playBtn;
    private Slider progress;
    private Label timeLabel;
    private Slider volumeSlider;
    private Button queueBtn;
    private Button detachBtn;

    // Callbacks
    private Runnable onQueueToggle;
    private Runnable onNowPlayingClick;
    private Runnable onDetachAction;

    public MiniPlayerBar() {
        setPrefHeight(90);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1 0 0 0; -fx-padding: 10 30;");
        setSpacing(20);

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        // --- 1. Track Info (Left) ---
        coverView = new ImageView();
        coverView.setFitHeight(60);
        coverView.setFitWidth(60);
        coverView.setPreserveRatio(true);
        coverView.setSmooth(true);
        coverView.setCursor(Cursor.HAND);
        coverView.setOnMouseClicked(e -> {
            if (onNowPlayingClick != null) onNowPlayingClick.run();
        });

        VBox trackInfo = new VBox(5);
        trackInfo.setPrefWidth(250);
        trackTitle = new Label("-");
        trackTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        trackArtist = new Label("-");
        trackArtist.setStyle("-fx-text-fill: #a1a1aa;");
        trackInfo.getChildren().addAll(trackTitle, trackArtist);


        // --- 2. Center Controls (Progress + Buttons) ---
        HBox btns = new HBox(15);
        btns.setAlignment(Pos.CENTER);
        Button prevBtn = new Button("<<");
        playBtn = new Button("Play");
        Button nextBtn = new Button(">>");

        // Styling
        String btnStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #3f3f46; -fx-border-radius: 4;";
        prevBtn.setStyle(btnStyle);
        playBtn.setStyle(btnStyle);
        nextBtn.setStyle(btnStyle);

        prevBtn.setOnAction(e -> player.playPrevious());
        playBtn.setOnAction(e -> player.togglePlay());
        nextBtn.setOnAction(e -> player.playNext());

        btns.getChildren().addAll(prevBtn, playBtn, nextBtn);

        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        progress = new Slider();
        progress.setPrefWidth(300);
        timeLabel = new Label("0:00 / 0:00");
        timeLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 10px;");
        progressBox.getChildren().addAll(btns, progress, timeLabel);


        // --- 3. Right Controls (Volume + Queue + Detach) ---
        HBox rightControls = new HBox(10);
        rightControls.setAlignment(Pos.CENTER_RIGHT);
        
        Label volLabel = new Label("Vol");
        volLabel.setStyle("-fx-text-fill: #a1a1aa;");
        
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().bindBidirectional(player.volumeProperty());

        queueBtn = new Button("Queue");
        queueBtn.setStyle(btnStyle);
        queueBtn.setOnAction(e -> {
            if (onQueueToggle != null) onQueueToggle.run();
        });
        
        detachBtn = new Button("↗"); // Detach icon
        detachBtn.setStyle(btnStyle + " -fx-font-size: 14px;");
        detachBtn.setTooltip(new javafx.scene.control.Tooltip("Switch to Floating Player"));
        detachBtn.setOnAction(e -> {
            if (onDetachAction != null) onDetachAction.run();
        });

        rightControls.getChildren().addAll(volLabel, volumeSlider, queueBtn, detachBtn);

        // Add all sections to bar
        getChildren().addAll(coverView, trackInfo, progressBox, rightControls);
        HBox.setHgrow(progressBox, Priority.ALWAYS);
        HBox.setHgrow(rightControls, Priority.NEVER);
    }

    private void setupListeners() {
        // Track Updates
        player.currentTrackProperty().addListener((obs, old, track) -> {
            if (track != null) {
                trackTitle.setText(track.getTitle());
                trackArtist.setText(track.getArtist());
                Image art = CoverArtService.getInstance().getCoverArt(track);
                coverView.setImage(art);
            }
        });

        // Playback State
        player.isPlayingProperty().addListener((obs, old, playing) -> {
            playBtn.setText(playing ? "Pause" : "Play");
        });

        // Time / Progress
        player.currentTimeProperty().addListener((obs, old, time) -> {
            if (!progress.isValueChanging()) {
                progress.setValue(time.doubleValue());
            }
            timeLabel.setText(formatTime(time.doubleValue()) + " / " + formatTime(player.durationProperty().get()));
        });

        player.durationProperty().addListener((obs, old, dur) -> {
            progress.setMax(dur.doubleValue());
        });

        // Seek Logic
        setupSeekBehavior();
    }

    private void setupSeekBehavior() {
        final boolean[] isDragging = new boolean[] { false };
        final boolean[] wasPlayingDuringDrag = new boolean[] { false };

        progress.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (isChanging) {
                isDragging[0] = true;
                wasPlayingDuringDrag[0] = player.isPlayingProperty().get();
                if (wasPlayingDuringDrag[0]) player.pause();
            } else {
                if (isDragging[0]) {
                    player.seek(progress.getValue());
                    if (wasPlayingDuringDrag[0]) player.play();
                    isDragging[0] = false;
                }
            }
        });

        progress.setOnMousePressed(e -> {
            isDragging[0] = true;
            wasPlayingDuringDrag[0] = player.isPlayingProperty().get();
            if (wasPlayingDuringDrag[0]) player.pause();
        });

        progress.setOnMouseReleased(e -> {
            if (!progress.isValueChanging()) {
                player.seek(progress.getValue());
                if (wasPlayingDuringDrag[0]) player.play();
                isDragging[0] = false;
            }
        });
    }

    private String formatTime(double seconds) {
        int m = (int) seconds / 60;
        int s = (int) seconds % 60;
        return String.format("%d:%02d", m, s);
    }
    
    // Setters for external actions
    public void setOnQueueToggle(Runnable action) { this.onQueueToggle = action; }
    public void setOnNowPlayingClick(Runnable action) { this.onNowPlayingClick = action; }
    public void setOnDetachAction(Runnable action) { this.onDetachAction = action; }
    
    // UI State helpers
    public void setQueueActive(boolean active) {
         if (active) {
             queueBtn.setStyle("-fx-background-color: #3f3f46; -fx-text-fill: white; -fx-border-color: #3f3f46; -fx-border-radius: 4;");
         } else {
             queueBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #3f3f46; -fx-border-radius: 4;");
         }
    }
}
