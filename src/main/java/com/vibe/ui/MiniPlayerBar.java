package com.vibe.ui;

import com.vibe.PlayerController;
import com.vibe.service.CoverArtService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Cursor;
import javafx.scene.shape.Rectangle;

public class MiniPlayerBar extends BorderPane {

    private PlayerController player = PlayerController.getInstance();
    private ImageView coverView;
    private Label trackTitle;
    private Label trackArtist;
    private Button playBtn;
    private Slider progress;
    private Label timeLabel;
    private Label totalTimeLabel; // Promoted field
    private Slider volumeSlider;
    private Button queueBtn;
    private Button detachBtn;

    // Callbacks
    private Runnable onQueueToggle;
    private Runnable onNowPlayingClick;
    private Runnable onDetachAction;

    public MiniPlayerBar() {
        setPrefHeight(90);
        getStyleClass().add("player-bar");

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        // --- 1. Track Info (Left) ---
        HBox trackSection = new HBox(16);
        trackSection.setAlignment(Pos.CENTER_LEFT);

        coverView = new ImageView();
        coverView.setFitHeight(56);
        coverView.setFitWidth(56);
        coverView.setPreserveRatio(true);
        coverView.setSmooth(true);
        coverView.setCursor(Cursor.HAND);

        Rectangle clip = new Rectangle(56, 56);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        coverView.setClip(clip);

        coverView.setOnMouseClicked(e -> {
            if (onNowPlayingClick != null)
                onNowPlayingClick.run();
        });

        VBox trackInfo = new VBox(4);
        trackInfo.setAlignment(Pos.CENTER_LEFT);

        trackTitle = new Label("No Track");
        trackTitle.getStyleClass().add("label");
        trackTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        trackArtist = new Label("Select a song");
        trackArtist.getStyleClass().add("label-secondary");

        trackInfo.getChildren().addAll(trackTitle, trackArtist);
        trackSection.getChildren().addAll(coverView, trackInfo);

        // Wrap Left in a container with fixed width to balance the Right side
        StackPane leftContainer = new StackPane(trackSection);
        leftContainer.setAlignment(Pos.CENTER_LEFT);
        leftContainer.setPrefWidth(300); // FIXED WIDTH
        leftContainer.setMinWidth(300);
        leftContainer.setMaxWidth(300);

        // --- 2. Center Controls (Progress + Buttons) ---
        VBox centerSection = new VBox(8);
        centerSection.setAlignment(Pos.CENTER);

        HBox btns = new HBox(24);
        btns.setAlignment(Pos.CENTER);

        Button shuffleBtn = new Button("🔀");
        shuffleBtn.getStyleClass().add("player-btn");
        shuffleBtn.setStyle("-fx-font-size: 14px;"); // Smaller secondary icon

        Button prevBtn = new Button("⏮");
        prevBtn.getStyleClass().add("player-btn");

        playBtn = new Button("▶");
        playBtn.getStyleClass().add("circle-play-btn"); // Large play button

        Button nextBtn = new Button("⏭");
        nextBtn.getStyleClass().add("player-btn");

        Button repeatBtn = new Button("🔁");
        repeatBtn.getStyleClass().add("player-btn");
        repeatBtn.setStyle("-fx-font-size: 14px;");

        prevBtn.setOnAction(e -> player.playPrevious());
        playBtn.setOnAction(e -> player.togglePlay());
        nextBtn.setOnAction(e -> player.playNext());

        btns.getChildren().addAll(shuffleBtn, prevBtn, playBtn, nextBtn, repeatBtn);

        HBox progressBox = new HBox(12);
        progressBox.setAlignment(Pos.CENTER);

        Label currentTime = new Label("0:00");
        currentTime.getStyleClass().add("label-tertiary");
        currentTime.setMinWidth(40);
        currentTime.setAlignment(Pos.CENTER_RIGHT);

        progress = new Slider();
        progress.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progress, Priority.ALWAYS);
        progress.setMinWidth(300); // Ensure minimal width for slider

        totalTimeLabel = new Label("0:00");
        totalTimeLabel.getStyleClass().add("label-tertiary");
        totalTimeLabel.setMinWidth(40);
        totalTimeLabel.setAlignment(Pos.CENTER_LEFT);

        progressBox.getChildren().addAll(currentTime, progress, totalTimeLabel);
        timeLabel = currentTime;

        centerSection.getChildren().addAll(btns, progressBox);

        // --- 3. Right Controls (Volume + Queue + Detach) ---
        HBox rightControls = new HBox(16);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        Label volIcon = new Label("🔊");
        volIcon.getStyleClass().add("label-secondary");

        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().bindBidirectional(player.volumeProperty());

        queueBtn = new Button("☰");
        queueBtn.getStyleClass().add("player-btn");
        queueBtn.setStyle("-fx-font-size: 16px;");
        queueBtn.setOnAction(e -> {
            if (onQueueToggle != null)
                onQueueToggle.run();
        });

        detachBtn = new Button("↗");
        detachBtn.getStyleClass().add("player-btn");
        detachBtn.setStyle("-fx-font-size: 14px;");
        detachBtn.setOnAction(e -> {
            if (onDetachAction != null)
                onDetachAction.run();
        });

        rightControls.getChildren().addAll(volIcon, volumeSlider, queueBtn, detachBtn);

        // Wrap Right in a container with SAME fixed width as Left
        StackPane rightContainer = new StackPane(rightControls);
        rightContainer.setAlignment(Pos.CENTER_RIGHT);
        rightContainer.setPrefWidth(300); // FIXED WIDTH MATCHING LEFT
        rightContainer.setMinWidth(300);
        rightContainer.setMaxWidth(300);

        // --- Assembly ---
        setLeft(leftContainer);
        setCenter(centerSection);
        setRight(rightContainer);
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
            playBtn.setText(playing ? "⏸" : "▶");
        });

        // Time / Progress
        player.currentTimeProperty().addListener((obs, old, time) -> {
            if (!progress.isValueChanging()) {
                progress.setValue(time.doubleValue());
            }
            timeLabel.setText(formatTime(time.doubleValue()));
        });

        player.durationProperty().addListener((obs, old, dur) -> {
            progress.setMax(dur.doubleValue());
            totalTimeLabel.setText(formatTime(dur.doubleValue()));
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
                if (wasPlayingDuringDrag[0])
                    player.pause();
            } else {
                if (isDragging[0]) {
                    player.seek(progress.getValue());
                    if (wasPlayingDuringDrag[0])
                        player.play();
                    isDragging[0] = false;
                }
            }
        });

        progress.setOnMousePressed(e -> {
            isDragging[0] = true;
            wasPlayingDuringDrag[0] = player.isPlayingProperty().get();
            if (wasPlayingDuringDrag[0])
                player.pause();
        });

        progress.setOnMouseReleased(e -> {
            if (!progress.isValueChanging()) {
                player.seek(progress.getValue());
                if (wasPlayingDuringDrag[0])
                    player.play();
                isDragging[0] = false;
            }
        });
    }

    // Setters for external actions
    public void setOnQueueToggle(Runnable action) {
        this.onQueueToggle = action;
    }

    public void setOnNowPlayingClick(Runnable action) {
        this.onNowPlayingClick = action;
    }

    public void setOnDetachAction(Runnable action) {
        this.onDetachAction = action;
    }

    // UI State helpers
    public void setQueueActive(boolean active) {
        if (active) {
            queueBtn.getStyleClass().add("active");
            queueBtn.setStyle("-fx-text-fill: white;");
        } else {
            queueBtn.getStyleClass().remove("active");
            queueBtn.setStyle("");
        }
    }

    private String formatTime(double seconds) {
        if (Double.isNaN(seconds) || seconds < 0)
            return "0:00";
        int s = (int) seconds;
        int m = s / 60;
        s = s % 60;
        return String.format("%d:%02d", m, s);
    }
}
