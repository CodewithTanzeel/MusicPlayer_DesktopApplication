package com.vibe.ui;

import com.vibe.Main;
import com.vibe.db.DatabaseManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScene {

    public Parent getView(Stage stage) {
        BorderPane root = new BorderPane();

        // Custom Title Bar
        root.setTop(new WindowControls(stage));

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 50;");

        Label title = new Label("Vibe");
        title.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: linear-gradient(to right, #a78bfa, #f472b6);");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");
        usernameInput.setMaxWidth(300);

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");
        passwordInput.setMaxWidth(300);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginBtn = new Button("Log In");
        loginBtn.setMaxWidth(300);
        loginBtn.setOnAction(e -> {
            String user = usernameInput.getText().trim();
            String pass = passwordInput.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please enter username and password");
                return;
            }

            boolean success = DatabaseManager.loginUser(user, pass);
            if (success) {
                MainScene mainScene = new MainScene();
                Main.setScene(new Scene(mainScene.getView(Main.getStage()), 1280, 800));
            } else {
                errorLabel.setText("Invalid credentials");
            }
        });

        Button registerBtn = new Button("Register");
        registerBtn.setMaxWidth(300);
        registerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a78bfa;");
        registerBtn.setOnAction(e -> {
            String user = usernameInput.getText().trim();
            String pass = passwordInput.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Username and password cannot be empty");
                errorLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            boolean success = DatabaseManager.registerUser(user, pass);
            if (success) {
                errorLabel.setText("Registered! Please login.");
                errorLabel.setStyle("-fx-text-fill: green;");
            } else {
                errorLabel.setText("User already exists");
                errorLabel.setStyle("-fx-text-fill: red;");
            }
        });

        layout.getChildren().addAll(title, usernameInput, passwordInput, loginBtn, registerBtn, errorLabel);

        root.setCenter(layout);
        return root;
    }
}
