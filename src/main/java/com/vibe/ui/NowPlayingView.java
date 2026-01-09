package com.vibe.ui;

import com.vibe.PlayerController;
import com.vibe.model.Track;
import com.vibe.service.CoverArtService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class NowPlayingView extends StackPane {

    private PlayerController player = PlayerController.getInstance();
    private ImageView bgImageView;
    private ImageView coverImageView;
    private Label titleLabel;
    private Label artistLabel;
    private Label albumLabel;
    private Runnable onBackAction;

    public NowPlayingView() {
        setStyle("-fx-background-color: #000;");

        // 1. Background (Blurred)
        bgImageView = new ImageView();
        bgImageView.fitWidthProperty().bind(widthProperty());
        bgImageView.fitHeightProperty().bind(heightProperty());
        bgImageView.setPreserveRatio(false); // Stretch
        bgImageView.setOpacity(0.3);
        bgImageView.setEffect(new BoxBlur(40, 40, 3));

        // 2. Main Content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        
        // Cover Art Card
        coverImageView = new ImageView();
        coverImageView.setFitWidth(350);
        coverImageView.setFitHeight(350);
        coverImageView.setPreserveRatio(true);
        coverImageView.setSmooth(true);
        // Shadow for depth
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setRadius(30);
        coverImageView.setEffect(shadow);

        // Metadata
        titleLabel = new Label("Title");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        artistLabel = new Label("Artist");
        artistLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #e4e4e7;");
        
        albumLabel = new Label("Album");
        albumLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #a1a1aa;");

        // Back Button
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            if (onBackAction != null) onBackAction.run();
        });
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 20;");

        content.getChildren().addAll(coverImageView, titleLabel, artistLabel, albumLabel, backBtn);

        getChildren().addAll(bgImageView, content);

        // Update on load
        update(player.currentTrackProperty().get());

        // Listener
        player.currentTrackProperty().addListener((obs, old, track) -> {
            update(track);
        });
    }

    private void update(Track track) {
        if (track == null) return;
        
        titleLabel.setText(track.getTitle());
        artistLabel.setText(track.getArtist());
        albumLabel.setText(track.getAlbum());

        Image art = CoverArtService.getInstance().getCoverArt(track);
        if (art == null) {
            // Placeholder can be a solid color or default resource
            // For now, we leave it null (transparent) or reuse previous?
            // Better to set null if no art found to clear previous
            coverImageView.setImage(null);
            bgImageView.setImage(null); 
        } else {
            coverImageView.setImage(art);
            bgImageView.setImage(art);
        }
    }

    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }
}
