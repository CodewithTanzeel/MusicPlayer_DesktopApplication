package com.vibe.ui;

import com.vibe.Main;
import com.vibe.db.DatabaseManager;
import com.vibe.model.Track;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executors;

public class SetupWizardScene {

    private BorderPane root;
    private Stage stage;

    public Parent getView(Stage stage) {
        this.stage = stage;
        root = new BorderPane();
        root.setTop(new WindowControls(stage)); // Keep the custom window controls

        showWelcomeStep();

        return root;
    }

    private void showWelcomeStep() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        Label title = new Label("Welcome to Vibe");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("The ultimate music experience for your desktop.");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #a1a1aa;");

        Button nextBtn = new Button("Get Started");
        nextBtn.getStyleClass().add("button-primary"); 
        nextBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10 30; -fx-background-color: #a78bfa; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        nextBtn.setOnAction(e -> showLibraryStep());

        layout.getChildren().addAll(title, subtitle, nextBtn);
        animateTransition(layout);
        root.setCenter(layout);
    }

    private void showLibraryStep() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        Label title = new Label("Where is your music?");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Select the folder where you keep your audio files.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a1aa;");

        Button chooseBtn = new Button("Choose Folder");
        chooseBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-background-color: #3f3f46; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #a1a1aa;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(300);

        Button nextBtn = new Button("Skip / Custom Later");
        nextBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #71717a; -fx-cursor: hand;");
        nextBtn.setOnAction(e -> showFinishStep());

        chooseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Music Directory");
            File dir = chooser.showDialog(stage);
            if (dir != null) {
                // Run scan in background
                progressBar.setVisible(true);
                progressBar.setProgress(-1); // Indeterminate
                statusLabel.setText("Scanning " + dir.getName() + "...");
                
                Executors.newSingleThreadExecutor().submit(() -> {
                    scanDirectory(dir);
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(1);
                        statusLabel.setText("Scan Complete!");
                        nextBtn.setText("Continue");
                        nextBtn.setStyle("-fx-background-color: #a78bfa; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
                        nextBtn.setOnAction(ev -> showFinishStep());
                    });
                });
            }
        });

        layout.getChildren().addAll(title, subtitle, chooseBtn, progressBar, statusLabel, nextBtn);
        animateTransition(layout);
        root.setCenter(layout);
    }
    
    private void showFinishStep() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        Label title = new Label("All Set!");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Vibe is ready to play.");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #a1a1aa;");

        Button finishBtn = new Button("Start Listening");
        finishBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12 40; -fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        finishBtn.setOnAction(e -> {
           // Complete Setup
           DatabaseManager.setSetting("setup_completed", "true");
           
           // Go to Login
           LoginScene login = new LoginScene();
           Main.setScene(new Scene(login.getView(Main.getStage()), 1280, 720));
        });

        layout.getChildren().addAll(title, subtitle, finishBtn);
        animateTransition(layout);
        root.setCenter(layout);
    }

    private void animateTransition(Parent content) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void scanDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f);
            } else {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".flac")) {
                     Track t = new Track(UUID.randomUUID().toString(), f.getAbsolutePath(), f.getName(), "Unknown Artist",
                        "Unknown Album", 0);
                    DatabaseManager.addTrack(t);
                }
            }
        }
    }
}
