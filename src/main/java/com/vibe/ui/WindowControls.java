package com.vibe.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class WindowControls extends HBox {
    
    private double xOffset = 0;
    private double yOffset = 0;

    public WindowControls(Stage stage) {
        this.setAlignment(Pos.CENTER_RIGHT);
        this.setPrefHeight(32);
        this.setStyle("-fx-background-color: #0f0f13; -fx-padding: 0 10 0 10;");

        // Drag Logic
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        spacer.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
            
            // Re-maximize if dragged from max state? (Advanced, skipping for now)
        });

        // Buttons
        Button minBtn = createButton("_");
        minBtn.setOnAction(e -> stage.setIconified(true));

        Button maxBtn = createButton("□");
        maxBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

        Button closeBtn = createButton("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: bold;"));
        closeBtn.setOnAction(e -> stage.close());

        this.getChildren().addAll(spacer, minBtn, maxBtn, closeBtn);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-font-size: 14px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-font-size: 14px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-font-size: 14px;"));
        return btn;
    }
}
