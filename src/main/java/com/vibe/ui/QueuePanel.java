package com.vibe.ui;

import com.vibe.PlayerController;
import com.vibe.model.Track;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.util.Collections;
import java.util.List;

public class QueuePanel extends VBox {

    private PlayerController player = PlayerController.getInstance();
    private ListView<Track> queueList;

    public QueuePanel() {
        setPadding(new Insets(20));
        setSpacing(10);
        setPrefWidth(300);
        setStyle("-fx-background-color: #1a1a1d; -fx-border-color: #333; -fx-border-width: 0 0 0 1;");

        Label header = new Label("Queue");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        queueList = new ListView<>();
        queueList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        VBox.setVgrow(queueList, Priority.ALWAYS);
        
        setupList();
        refresh();

        getChildren().addAll(header, queueList);
    }

    public void refresh() {
        queueList.getItems().setAll(player.getQueueList());
    }

    private void setupList() {
        queueList.setCellFactory(param -> new QueueListCell());
    }

    private class QueueListCell extends ListCell<Track> {
        private HBox content;
        private Label title;
        private Label artist;
        private Button removeBtn;

        public QueueListCell() {
            content = new HBox(10);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(5));
            
            VBox info = new VBox(2);
            title = new Label();
            title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            artist = new Label();
            artist.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
            info.getChildren().addAll(title, artist);
            HBox.setHgrow(info, Priority.ALWAYS);

            removeBtn = new Button("x");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e66; -fx-font-weight: bold;");
            removeBtn.setOnAction(e -> {
                Track item = getItem();
                if (item != null) {
                    int idx = getIndex();
                    if (idx >= 0 && idx < player.getQueueList().size()) {
                        player.removeFromQueue(idx);
                        refresh();
                    }
                }
            });

            content.getChildren().addAll(info, removeBtn);
            
            // Drag and Drop Logic
            setOnDragDetected(event -> {
                if (getItem() == null) return;
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(getIndex()));
                db.setContent(cc);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    setOpacity(1);
                }
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int fromIndex = Integer.parseInt(db.getString());
                    int toIndex = getIndex();
                    
                    if (toIndex < 0) toIndex = queueList.getItems().size() - 1; 
                    
                    player.moveQueueItem(fromIndex, toIndex);
                    refresh();
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }

        @Override
        protected void updateItem(Track item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                title.setText(item.getTitle());
                artist.setText(item.getArtist());
                setGraphic(content);
            }
        }
    }
}
