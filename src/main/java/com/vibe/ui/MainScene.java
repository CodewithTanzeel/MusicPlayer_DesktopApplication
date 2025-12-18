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

        // Playlists header (button + caret)
        Button playlistsBtn = new Button("Playlists");
        playlistsBtn.setMaxWidth(Double.MAX_VALUE);
        Label caretLabel = new Label("\u25BE");
        caretLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        HBox playlistsHeader = new HBox(8, playlistsBtn, caretLabel);
        playlistsHeader.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(playlistsBtn, Priority.ALWAYS);

        // Dropdown content
        VBox playlistDropdown = new VBox(6);
        playlistDropdown.setStyle("-fx-padding: 6 0 0 0;");

        // Scroll area (start collapsed)
        ScrollPane playlistScroll = new ScrollPane(playlistDropdown);
        playlistScroll.setFitToWidth(true);
        playlistScroll.setPrefViewportHeight(0);
        playlistScroll.setMaxHeight(0);
        playlistScroll.setMinHeight(0);
        playlistScroll.setStyle("-fx-background-color: transparent; -fx-padding: 4 0 0 0;");
        playlistScroll.setVisible(false);
        playlistScroll.setManaged(false);

        // Animation settings
        final double expandedHeight = 220.0;
        final javafx.util.Duration animDur = javafx.util.Duration.millis(220);

        playlistsHeader.setOnMouseClicked(e -> {
            boolean opening = playlistScroll.getMaxHeight() == 0;
            if (opening) {
                rebuildPlaylistDropdown(playlistDropdown, root);
                playlistScroll.setVisible(true);
                playlistScroll.setManaged(true);

                javafx.animation.Timeline tl = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                                new javafx.animation.KeyValue(playlistScroll.maxHeightProperty(), 0),
                                new javafx.animation.KeyValue(playlistScroll.prefViewportHeightProperty(), 0),
                                new javafx.animation.KeyValue(caretLabel.rotateProperty(), 0)
                        ),
                        new javafx.animation.KeyFrame(animDur,
                                new javafx.animation.KeyValue(playlistScroll.maxHeightProperty(), expandedHeight),
                                new javafx.animation.KeyValue(playlistScroll.prefViewportHeightProperty(), expandedHeight - 20),
                                new javafx.animation.KeyValue(caretLabel.rotateProperty(), 180)
                        )
                );
                tl.play();
            } else {
                javafx.animation.Timeline tl = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                                new javafx.animation.KeyValue(playlistScroll.maxHeightProperty(), playlistScroll.getMaxHeight()),
                                new javafx.animation.KeyValue(playlistScroll.prefViewportHeightProperty(), playlistScroll.getPrefViewportHeight()),
                                new javafx.animation.KeyValue(caretLabel.rotateProperty(), 180)
                        ),
                        new javafx.animation.KeyFrame(animDur,
                                new javafx.animation.KeyValue(playlistScroll.maxHeightProperty(), 0),
                                new javafx.animation.KeyValue(playlistScroll.prefViewportHeightProperty(), 0),
                                new javafx.animation.KeyValue(caretLabel.rotateProperty(), 0)
                        )
                );
                tl.setOnFinished(ev2 -> {
                    playlistScroll.setVisible(false);
                    playlistScroll.setManaged(false);
                });
                tl.play();
            }
        });

        sidebar.getChildren().addAll(brand, libraryBtn, playlistsHeader, playlistScroll);
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

        // Seek behavior: pause during drag and seek/resume on release
        final boolean[] isDragging = new boolean[] { false };
        final boolean[] wasPlayingDuringDrag = new boolean[] { false };

        progress.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (isChanging) {
                // Drag started
                isDragging[0] = true;
                wasPlayingDuringDrag[0] = player.isPlayingProperty().get();
                if (wasPlayingDuringDrag[0]) player.pause();
            } else {
                // Drag ended
                if (isDragging[0]) {
                    player.seek(progress.getValue());
                    if (wasPlayingDuringDrag[0]) player.play();
                    isDragging[0] = false;
                    wasPlayingDuringDrag[0] = false;
                }
            }
        });

        progress.setOnMousePressed(e -> {
            // Start drag (mouse)
            isDragging[0] = true;
            wasPlayingDuringDrag[0] = player.isPlayingProperty().get();
            if (wasPlayingDuringDrag[0]) player.pause();
        });

        progress.setOnMouseReleased(e -> {
            // Click-to-jump or mouse release after minor move: if not currently considered a changing drag
            if (!progress.isValueChanging()) {
                player.seek(progress.getValue());
                if (wasPlayingDuringDrag[0]) player.play();
                isDragging[0] = false;
                wasPlayingDuringDrag[0] = false;
            }
        });

        // Note: removed seek-on-key-release to avoid preview behavior while using keyboard adjustments.

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

        VBox container = new VBox(12);
        container.setFillWidth(true);

        for (Playlist p : DatabaseManager.getAllPlaylists()) {
            VBox card = new VBox(8);
            card.setStyle("-fx-padding: 12; -fx-background-color: #0b0b0d; -fx-background-radius: 8; -fx-border-radius: 8;");
            card.setMaxWidth(Double.MAX_VALUE);

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setMaxWidth(Double.MAX_VALUE);

            Label name = new Label(p.getName());
            name.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");
            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, Priority.ALWAYS);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Delete button
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: white; -fx-font-size: 12px;");

            // Drop-down caret button embedded in the card
            Button caretBtn = new Button("\u25BE"); // down triangle
            caretBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-font-size: 14px; -fx-padding: 2 6 2 6; -fx-background-radius: 6;");
            caretBtn.setRotate(0);
            caretBtn.setFocusTraversable(false);

            header.getChildren().addAll(name, spacer, deleteBtn, caretBtn);
            card.getChildren().add(header);

            // Expanded area (hidden by default)
            VBox expanded = new VBox(8);
            expanded.setStyle("-fx-padding: 6 0 0 0;");
            TableView<Track> table = new TableView<>();
            table.setMaxHeight(200);
            setupPlaylistTableColumns(table, p.getId());
            table.getItems().setAll(DatabaseManager.getTracksForPlaylist(p.getId()));
            table.setVisible(false);
            table.setManaged(false);

            HBox actions = new HBox(8);
            Button openFull = new Button("Open full view");
            Button closeBtn = new Button("Close");
            openFull.setOnAction(ev -> showPlaylistTracks(p, root));
            closeBtn.setOnAction(ev -> {
                table.setVisible(false);
                table.setManaged(false);
                // rotate caret back
                javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(javafx.util.Duration.millis(180), caretBtn);
                rt.setToAngle(0);
                rt.play();
            });
            actions.getChildren().addAll(openFull, closeBtn);

            expanded.getChildren().addAll(table, actions);
            card.getChildren().add(expanded);

            // Delete action
            deleteBtn.setOnAction(ev -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete playlist '" + p.getName() + "'?", ButtonType.OK, ButtonType.CANCEL);
                Optional<ButtonType> res = confirm.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.OK) {
                    boolean ok = DatabaseManager.deletePlaylist(p.getId());
                    if (ok) {
                        // refresh the playlists view
                        showPlaylists(root);
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete playlist");
                        err.showAndWait();
                    }
                }
            });

            // Toggle only when caret button is clicked (not whole header)
            caretBtn.setOnAction(e -> {
                boolean opening = !table.isVisible();

                // Collapse any other expanded cards and reset their carets
                for (javafx.scene.Node n : container.getChildren()) {
                    if (n instanceof VBox) {
                        VBox other = (VBox) n;
                        if (other != card && other.getChildren().size() > 1) {
                            javafx.scene.Node maybeTable = other.getChildren().get(1).lookup(".table-view");
                            if (maybeTable instanceof TableView) {
                                TableView<?> otherTable = (TableView<?>) maybeTable;
                                otherTable.setVisible(false);
                                otherTable.setManaged(false);
                                javafx.scene.Node otherCaret = ((HBox) other.getChildren().get(0)).getChildren().get(3);
                                if (otherCaret instanceof Button) {
                                    javafx.animation.RotateTransition rto = new javafx.animation.RotateTransition(javafx.util.Duration.millis(180), (Button) otherCaret);
                                    rto.setToAngle(0);
                                    rto.play();
                                }
                            }
                        }
                    }
                }

                if (opening) {
                    table.getItems().setAll(DatabaseManager.getTracksForPlaylist(p.getId()));
                    table.setVisible(true);
                    table.setManaged(true);
                    javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(javafx.util.Duration.millis(180), caretBtn);
                    rt.setToAngle(180);
                    rt.play();
                } else {
                    table.setVisible(false);
                    table.setManaged(false);
                    javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(javafx.util.Duration.millis(180), caretBtn);
                    rt.setToAngle(0);
                    rt.play();
                }
            });

            container.getChildren().add(card);
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(420);
        scroll.setStyle("-fx-background-color: transparent; -fx-padding: 4;");

        playlistsView.getChildren().addAll(pageTitle, scroll);
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
        setupPlaylistTableColumns(table, playlist.getId());
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

    private void rebuildPlaylistDropdown(VBox playlistDropdown, BorderPane root) {
        playlistDropdown.getChildren().clear();
        for (Playlist pl : DatabaseManager.getAllPlaylists()) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);

            Button plBtn = new Button(pl.getName());
            plBtn.setMaxWidth(Double.MAX_VALUE);
            plBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left;");
            HBox.setHgrow(plBtn, Priority.ALWAYS);
            plBtn.setOnAction(ev -> {
                // Open playlist in main content but keep dropdown visible so items don't disappear when clicked
                showPlaylistTracks(pl, root);

                // Update selection visual: clear others and highlight this one
                for (javafx.scene.Node node : playlistDropdown.getChildren()) {
                    if (node instanceof HBox) {
                        HBox h = (HBox) node;
                        if (!h.getChildren().isEmpty() && h.getChildren().get(0) instanceof Button) {
                            ((Button) h.getChildren().get(0)).setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left;");
                        }
                    }
                }
                plBtn.setStyle("-fx-background-color: #1f2937; -fx-text-fill: white; -fx-alignment: center-left;");
            });

            Button del = new Button("Delete");
            del.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: white; -fx-font-size: 11px;");
            del.setOnAction(ev -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete playlist '" + pl.getName() + "'?", ButtonType.OK, ButtonType.CANCEL);
                Optional<ButtonType> res = confirm.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.OK) {
                    boolean ok = DatabaseManager.deletePlaylist(pl.getId());
                    if (ok) {
                        rebuildPlaylistDropdown(playlistDropdown, root);
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete playlist");
                        err.showAndWait();
                    }
                }
            });

            item.getChildren().addAll(plBtn, del);
            playlistDropdown.getChildren().add(item);
        }
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

    private void setupPlaylistTableColumns(TableView<Track> table, String playlistId) {
        TableColumn<Track, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Track, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        artistCol.setPrefWidth(150);

        TableColumn<Track, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        albumCol.setPrefWidth(150);

        TableColumn<Track, Void> actionCol = new TableColumn<>("");
        actionCol.setPrefWidth(90);
        Callback<TableColumn<Track, Void>, TableCell<Track, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Track, Void> call(final TableColumn<Track, Void> param) {
                return new TableCell<>() {
                    private final Button removeBtn = new Button("Remove");
                    {
                        removeBtn.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: white;");
                        removeBtn.setOnAction(event -> {
                            Track t = getTableView().getItems().get(getIndex());
                            boolean ok = DatabaseManager.removeTrackFromPlaylist(playlistId, t.getId());
                            if (ok) {
                                getTableView().getItems().remove(t);
                            } else {
                                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to remove track from playlist");
                                err.showAndWait();
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(removeBtn);
                        }
                    }
                };
            }
        };
        actionCol.setCellFactory(cellFactory);

        table.getColumns().addAll(titleCol, artistCol, albumCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
