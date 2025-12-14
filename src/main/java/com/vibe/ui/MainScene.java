package com.vibe.ui;

import com.vibe.PlayerController;
import com.vibe.db.DatabaseManager;
import com.vibe.model.Track;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.UUID;

public class MainScene {

    private PlayerController player = PlayerController.getInstance();

    public Parent getView(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Custom Title Bar
        root.setTop(new WindowControls(stage));

        // --- Sidebar ---
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: #18181b; -fx-padding: 20;");
        
        Label brand = new Label("Vibe");
        brand.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Button libraryBtn = new Button("Library");
        libraryBtn.setMaxWidth(Double.MAX_VALUE);
        
        Button playlistsBtn = new Button("Playlists");
        playlistsBtn.setMaxWidth(Double.MAX_VALUE);
        
        sidebar.getChildren().addAll(brand, libraryBtn, playlistsBtn);
        root.setLeft(sidebar);

        // --- Center Content ---
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 30; -fx-background-color: #0f0f13;");
        
        // Define TableView first so we can pass it
        TableView<Track> table = new TableView<>();
        
        HBox header = new HBox(20);
        Label pageTitle = new Label("Library");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        
        Button importBtn = new Button("Import Folder");
        importBtn.setOnAction(e -> handleImport(root, table)); // Pass table to refresh it
        
        header.getChildren().addAll(pageTitle, importBtn);
        
        TableColumn<Track, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Track, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        artistCol.setPrefWidth(150);

        TableColumn<Track, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        albumCol.setPrefWidth(150);

        table.getColumns().addAll(titleCol, artistCol, albumCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        refreshLibrary(table);
        
        table.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    Track rowData = row.getItem();
                    player.setPlaylistContext(table.getItems(), rowData);
                }
            });
            return row ;
        });
        
        content.getChildren().addAll(header, table);
        root.setCenter(content);

        // --- Bottom Controls ---
        HBox controls = new HBox(20);
        controls.setPrefHeight(90);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1 0 0 0; -fx-padding: 10 30;");

        VBox trackInfo = new VBox(5);
        trackInfo.setPrefWidth(250);
        Label trackTitle = new Label("-");
        trackTitle.setStyle("-fx-font-weight: bold;");
        Label trackArtist = new Label("-");
        trackArtist.setStyle("-fx-text-fill: #a1a1aa;");
        trackInfo.getChildren().addAll(trackTitle, trackArtist);

        HBox btns = new HBox(15);
        btns.setAlignment(Pos.CENTER);
        Button prevBtn = new Button("<<");
        Button playBtn = new Button("Play");
        Button nextBtn = new Button(">>");
        
        prevBtn.setOnAction(e -> player.playPrevious());
        playBtn.setOnAction(e -> player.togglePlay());
        nextBtn.setOnAction(e -> player.playNext());
        
        btns.getChildren().addAll(prevBtn, playBtn, nextBtn);

        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        Slider progress = new Slider();
        progress.setPrefWidth(300);
        Label timeLabel = new Label("0:00 / 0:00");
        progressBox.getChildren().addAll(btns, progress, timeLabel);
        
        // Volume
        HBox volumeBox = new HBox(10);
        volumeBox.setAlignment(Pos.CENTER_RIGHT);
        Label volLabel = new Label("Vol");
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().bindBidirectional(player.volumeProperty());
        volumeBox.getChildren().addAll(volLabel, volumeSlider);

        controls.getChildren().addAll(trackInfo, progressBox, volumeBox);
        HBox.setHgrow(progressBox, Priority.ALWAYS);
        HBox.setHgrow(volumeBox, Priority.NEVER);
        
        root.setBottom(controls);
        
        // Listeners
        player.currentTrackProperty().addListener((obs, old, track) -> {
            if (track != null) {
                trackTitle.setText(track.getTitle());
                trackArtist.setText(track.getArtist());
            }
        });
        
        player.isPlayingProperty().addListener((obs, old, playing) -> {
            playBtn.setText(playing ? "Pause" : "Play");
        });
        
        player.currentTimeProperty().addListener((obs, old, time) -> {
            if (!progress.isValueChanging()) {
               progress.setValue(time.doubleValue());
            }
            timeLabel.setText(formatTime(time.doubleValue()) + " / " + formatTime(player.durationProperty().get()));
        });
        
        player.durationProperty().addListener((obs, old, dur) -> {
            progress.setMax(dur.doubleValue());
        });

        return root;
    }

    private void refreshLibrary(TableView<Track> table) {
        table.getItems().setAll(DatabaseManager.getAllTracks());
    }

    private void handleImport(Parent root, TableView<Track> table) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Import Folder");
        File dir = chooser.showDialog(root.getScene().getWindow());
        
        if (dir != null) {
            scanDirectory(dir);
            refreshLibrary(table); // Now works correct
        }
    }

    private void scanDirectory(File dir) {
       File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f);
            } else if (f.getName().endsWith(".mp3") || f.getName().endsWith(".wav")) {
                Track t = new Track(
                    UUID.randomUUID().toString(),
                    f.getAbsolutePath(),
                    f.getName(),
                    "Unknown Artist",
                    "Unknown Album",
                    0
                );
                DatabaseManager.addTrack(t);
            }
        }
    }
    
    private String formatTime(double seconds) {
        int m = (int) seconds / 60;
        int s = (int) seconds % 60;
        return String.format("%d:%02d", m, s);
    }
}
