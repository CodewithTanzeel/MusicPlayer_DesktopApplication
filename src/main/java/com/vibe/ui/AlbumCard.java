package com.vibe.ui;

import com.vibe.db.DatabaseManager;
import com.vibe.model.Playlist;
import com.vibe.model.Track;
import com.vibe.service.CoverArtService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class AlbumCard extends VBox {

    private final Track track;

    public AlbumCard(Track track, Runnable onClick) {
        this.track = track;

        this.setAlignment(Pos.TOP_LEFT);
        this.setSpacing(8);
        this.getStyleClass().add("album-card");
        this.setPrefWidth(180); // Standard width

        // Image Container
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(180, 180);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Image image = CoverArtService.getInstance().getCoverArt(track);
        imageView.setImage(image);

        Rectangle clip = new Rectangle(180, 180);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        imageView.setClip(clip);

        // Three dots menu button (Positioned top-right)
        MenuButton menuBtn = new MenuButton("⋯");
        // Flat style for menu button
        menuBtn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 0 6; -fx-font-weight: bold;");
        menuBtn.setVisible(false);
        StackPane.setAlignment(menuBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(menuBtn, new Insets(8));

        // Interaction
        imageContainer.setOnMouseEntered(e -> menuBtn.setVisible(true));
        imageContainer.setOnMouseExited(e -> {
            if (!menuBtn.isShowing())
                menuBtn.setVisible(false);
        });

        // Context Menu Items (Logic)
        MenuItem deleteItem = new MenuItem("Delete from Library");
        MenuItem addToPlaylistItem = new MenuItem("Add to Playlist");
        MenuItem createPlaylistItem = new MenuItem("Create New Playlist");

        setupMenuActions(deleteItem, addToPlaylistItem, createPlaylistItem, menuBtn);

        menuBtn.getItems().addAll(createPlaylistItem, addToPlaylistItem, deleteItem);
        imageContainer.getChildren().addAll(imageView, menuBtn);

        // Text Info
        Label titleLabel = new Label(track.getTitle());
        titleLabel.getStyleClass().add("label");
        titleLabel.setStyle("-fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);

        Label artistLabel = new Label(track.getArtist());
        artistLabel.getStyleClass().add("label-secondary");
        artistLabel.setWrapText(true);
        artistLabel.setMaxWidth(180);

        this.getChildren().addAll(imageContainer, titleLabel, artistLabel);

        // Click to play
        this.setOnMouseClicked(e -> {
            if (e.getTarget() != menuBtn && onClick != null) {
                onClick.run();
            }
        });
    }

    private void setupMenuActions(MenuItem delete, MenuItem add, MenuItem create, MenuButton btn) {
        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete '" + track.getTitle() + "'?\nThis will remove it from library and playlists.",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    DatabaseManager.deleteTrack(track.getId());
                    if (this.getParent() != null)
                        ((javafx.scene.layout.Pane) this.getParent()).getChildren().remove(this);
                }
            });
        });

        add.setOnAction(e -> showAddToPlaylistDialog());
        create.setOnAction(e -> showCreatePlaylistDialog());
    }

    private void showAddToPlaylistDialog() {
        Dialog<Playlist> dialog = new Dialog<>();
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Select a playlist for '" + track.getTitle() + "'");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        ListView<Playlist> lv = new ListView<>();
        lv.getItems().addAll(DatabaseManager.getAllPlaylists());
        dialog.getDialogPane().setContent(lv);

        dialog.setResultConverter(b -> b == addBtn ? lv.getSelectionModel().getSelectedItem() : null);

        dialog.showAndWait().ifPresent(pl -> {
            DatabaseManager.addTrackToPlaylist(pl.getId(), track.getId());
            new Alert(Alert.AlertType.INFORMATION, "Added to " + pl.getName()).showAndWait();
        });
    }

    private void showCreatePlaylistDialog() {
        TextInputDialog tid = new TextInputDialog();
        tid.setTitle("New Playlist");
        tid.setHeaderText("Create playlist with '" + track.getTitle() + "'");
        tid.showAndWait().ifPresent(name -> {
            if (!name.isEmpty()) {
                Playlist p = new Playlist(name);
                if (DatabaseManager.createPlaylist(p)) {
                    DatabaseManager.addTrackToPlaylist(p.getId(), track.getId());
                    new Alert(Alert.AlertType.INFORMATION, "Created " + name).showAndWait();
                }
            }
        });
    }
}
