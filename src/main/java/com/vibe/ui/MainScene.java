package com.vibe.ui;

import com.vibe.PlayerController;
import com.vibe.db.DatabaseManager;
import com.vibe.model.Track;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import com.vibe.model.Playlist;
import javafx.util.Callback;

public class MainScene {

    private PlayerController player = PlayerController.getInstance();

    private TableView<Track> libraryTable;
    private VBox libraryView;

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
        libraryBtn.setOnAction(e -> showLibrary(root));

        Button playlistsBtn = new Button("Playlists");
        playlistsBtn.setMaxWidth(Double.MAX_VALUE);
        playlistsBtn.setOnAction(e -> showPlaylists(root));

        sidebar.getChildren().addAll(brand, libraryBtn, playlistsBtn);
        root.setLeft(sidebar);

        // --- Center Content (Library Default) ---
        createLibraryView(root, stage);
        root.setCenter(libraryView);

        // --- Bottom Controls ---
        HBox controls = new HBox(20);
        controls.setPrefHeight(90);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle(
                "-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1 0 0 0; -fx-padding: 10 30;");

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
            refreshLibrary(table);
        }
    }

    private void scanDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File f : files) {
            String name = f.getName().toLowerCase();
            if (f.isDirectory()) {
                scanDirectory(f);
            } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")
                    || name.endsWith(".flac")) {
                System.out.println("Found track: " + f.getName());
                Track t = new Track(
                        UUID.randomUUID().toString(),
                        f.getAbsolutePath(),
                        f.getName(),
                        "Unknown Artist",
                        "Unknown Album",
                        0);
                DatabaseManager.addTrack(t);
            }
        }
    }

    private String formatTime(double seconds) {
        int m = (int) seconds / 60;
        int s = (int) seconds % 60;
        return String.format("%d:%02d", m, s);
    }

    private void createLibraryView(BorderPane root, Stage stage) {
        libraryView = new VBox(20);
        libraryView.setStyle("-fx-padding: 30; -fx-background-color: #0f0f13;");

        libraryTable = new TableView<>();

        HBox header = new HBox(20);
        Label pageTitle = new Label("Library");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        Button importBtn = new Button("Import Folder");
        importBtn.setOnAction(e -> handleImport(root, libraryTable));

        header.getChildren().addAll(pageTitle, importBtn);

        setupTableColumns(libraryTable);

        refreshLibrary(libraryTable);

        libraryTable.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Track rowData = row.getItem();
                    player.setPlaylistContext(libraryTable.getItems(), rowData);
                }
            });
            return row;
        });

        libraryView.getChildren().addAll(header, libraryTable);
    }

    private void setupTableColumns(TableView<Track> table) {
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

        // Context Menu Column
        TableColumn<Track, Void> actionCol = new TableColumn<>("");
        actionCol.setPrefWidth(50);
        Callback<TableColumn<Track, Void>, TableCell<Track, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Track, Void> call(final TableColumn<Track, Void> param) {
                return new TableCell<>() {
                    private final MenuButton btn = new MenuButton("...");
                    private final MenuItem createItem = new MenuItem("Create New Playlist");
                    private final MenuItem addItem = new MenuItem("Add to Existing Playlist");

                    {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
                        createItem.setOnAction(event -> {
                            Track track = getTableView().getItems().get(getIndex());
                            showCreatePlaylistDialog(track);
                        });
                        addItem.setOnAction(event -> {
                            Track track = getTableView().getItems().get(getIndex());
                            showAddToPlaylistDialog(track);
                        });
                        btn.getItems().addAll(createItem, addItem);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        // Reset graphic to null on empty
                        setGraphic(null);

                        if (!empty) {
                            setGraphic(btn);
                            // } else {
                            // setGraphic(null);
                        }
                    }
                };
            }
        };
        actionCol.setCellFactory(cellFactory);
        table.getColumns().add(actionCol);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void showLibrary(BorderPane root) {
        if (libraryView == null)
            return;
        root.setCenter(libraryView);
        refreshLibrary(libraryTable);
    }

    private void showPlaylists(BorderPane root) {
        VBox playlistsView = new VBox(20);
        playlistsView.setStyle("-fx-padding: 30; -fx-background-color: #0f0f13;");

        Label pageTitle = new Label("Playlists");
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        ListView<Playlist> list = new ListView<>();
        list.getItems().setAll(DatabaseManager.getAllPlaylists());
        list.setStyle("-fx-font-size: 16px;");

        list.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Playlist selected = list.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showPlaylistTracks(selected, root);
                }
            }
        });

        playlistsView.getChildren().addAll(pageTitle, list);
        root.setCenter(playlistsView);
    }

    private void showPlaylistTracks(Playlist playlist, BorderPane root) {
        VBox view = new VBox(20);
        view.setStyle("-fx-padding: 30; -fx-background-color: #0f0f13;");

        HBox header = new HBox(20);
        Label pageTitle = new Label(playlist.getName());
        pageTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> showPlaylists(root));

        header.getChildren().addAll(backBtn, pageTitle);

        TableView<Track> table = new TableView<>();
        setupTableColumns(table);
        table.getItems().setAll(DatabaseManager.getTracksForPlaylist(playlist.getId()));

        table.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Track rowData = row.getItem();
                    player.setPlaylistContext(table.getItems(), rowData);
                }
            });
            return row;
        });

        view.getChildren().addAll(header, table);
        root.setCenter(view);
    }

    private void showCreatePlaylistDialog(Track track) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Playlist");
        dialog.setHeaderText("Create a new playlist and add '" + track.getTitle() + "'");
        dialog.setContentText("Playlist Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.trim().isEmpty())
                return;

            if (DatabaseManager.checkPlaylistExists(name)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Playlist '" + name + "' already exists.");
                alert.showAndWait();
                return;
            }

            Playlist newPlaylist = new Playlist(name);
            if (DatabaseManager.createPlaylist(newPlaylist)) {
                DatabaseManager.addTrackToPlaylist(newPlaylist.getId(), track.getId());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Song added to the playlist: " + name);
                alert.showAndWait();
            }
        });
    }

    private void showAddToPlaylistDialog(Track track) {
        Dialog<Playlist> dialog = new Dialog<>();
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Select a playlist to add '" + track.getTitle() + "'");

        ButtonType addBtnType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtnType, ButtonType.CANCEL);

        ListView<Playlist> listView = new ListView<>();
        listView.getItems().addAll(DatabaseManager.getAllPlaylists());
        listView.setPrefHeight(200);
        listView.setPrefWidth(300);

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addBtnType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Playlist> result = dialog.showAndWait();
        result.ifPresent(playlist -> {
            DatabaseManager.addTrackToPlaylist(playlist.getId(), track.getId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Song added to the playlist: " + playlist.getName());
            alert.showAndWait();
        });
    }
}
