package com.vibe;

import com.vibe.db.DatabaseManager;
import com.vibe.ui.LoginScene;
import com.vibe.ui.ResizeHelper;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // Initialize DB
        DatabaseManager.initialize();
        PlayerController.getInstance().initialize();

        stage.initStyle(StageStyle.UNDECORATED); // Remove OS Window Frame
        stage.setTitle("MusicPlayer");
        
        String setupCompleted = DatabaseManager.getSetting("setup_completed");
        Scene scene;
        
        if ("true".equals(setupCompleted)) {
            // Start with Login
            LoginScene login = new LoginScene();
            scene = new Scene(login.getView(stage), 1280, 720);
        } else {
            // Start with Setup Wizard
            com.vibe.ui.SetupWizardScene wizard = new com.vibe.ui.SetupWizardScene();
            scene = new Scene(wizard.getView(stage), 1000, 700);
        }

        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        stage.setScene(scene);
        ResizeHelper.addResizeListener(stage); // Enable resizing
        stage.show();
    }
    
    public static void setScene(Scene scene) {
        primaryStage.setScene(scene);
        if (scene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        }
        ResizeHelper.addResizeListener(primaryStage); // Re-apply resize listener
    }
    
    public static Stage getStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
