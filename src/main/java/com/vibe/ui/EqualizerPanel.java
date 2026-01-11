package com.vibe.ui;

import com.vibe.PlayerController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class EqualizerPanel extends VBox {

    private final PlayerController player = PlayerController.getInstance();
    private final Slider[] sliders = new Slider[10];
    private final Label[] frequencyLabels = new Label[10];
    private final String[] frequencies = {"32Hz", "64Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz"};
    
    private ComboBox<String> presetCombo;
    private CheckBox enableToggle;

    private static final Map<String, double[]> PRESETS = new HashMap<>();

    static {
        PRESETS.put("Flat", new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        PRESETS.put("Pop", new double[]{2, 3, 4, 2, -1, -2, -1, 1, 2, 3});
        PRESETS.put("Rock", new double[]{4, 3, 2, 1, -1, -1, 1, 2, 3, 4});
        PRESETS.put("Jazz", new double[]{3, 2, 1, 1, 1, 2, 2, 3, 4, 4});
        PRESETS.put("Classical", new double[]{4, 3, 2, 1, 0, 0, 0, 1, 2, 2});
        PRESETS.put("Bass Boost", new double[]{6, 5, 4, 2, 0, 0, 0, 0, 0, 0});
        PRESETS.put("Treble Boost", new double[]{0, 0, 0, 0, 0, 0, 2, 4, 6, 8});
    }

    public EqualizerPanel() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #0f0f13;");
        setAlignment(Pos.TOP_CENTER);

        // Header
        Label title = new Label("Equalizer");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Controls Row
        HBox controls = new HBox(30);
        controls.setAlignment(Pos.CENTER);
        
        enableToggle = new CheckBox("Enable Equalizer");
        enableToggle.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        enableToggle.setSelected(player.isEqualizerEnabled());
        enableToggle.setOnAction(e -> player.setEqualizerEnabled(enableToggle.isSelected()));

        Label presetLabel = new Label("Preset:");
        presetLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 16px;");
        
        presetCombo = new ComboBox<>();
        presetCombo.getItems().addAll(PRESETS.keySet().stream().sorted().toList());
        presetCombo.setValue("Flat");
        presetCombo.setStyle("-fx-background-color: #1f2937; -fx-text-fill: white;");
        presetCombo.setOnAction(e -> applyPreset(presetCombo.getValue()));

        controls.getChildren().addAll(enableToggle, presetLabel, presetCombo);

        // Sliders Container
        HBox slidersBox = new HBox(15);
        slidersBox.setAlignment(Pos.CENTER);
        slidersBox.setPadding(new Insets(20, 0, 20, 0));

        double[] currentGains = player.getEqualizerGains();

        for (int i = 0; i < 10; i++) {
            VBox bandBox = new VBox(10);
            bandBox.setAlignment(Pos.CENTER);

            Slider slider = new Slider(-12, 12, currentGains[i]);
            slider.setOrientation(Orientation.VERTICAL);
            slider.setPrefHeight(250);
            slider.setShowTickMarks(true);
            slider.setShowTickLabels(false);
            slider.setMajorTickUnit(6);
            
            final int index = i;
            slider.valueProperty().addListener((obs, oldV, newV) -> {
                player.setEqualizerGain(index, newV.doubleValue());
                // If user moves slider, preset is custom
                if (!isApplyingPreset) {
                    presetCombo.setValue(null);
                }
            });

            Label freqLabel = new Label(frequencies[i]);
            freqLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
            
            Label gainLabel = new Label(String.format("%.1f dB", currentGains[i]));
            gainLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
            
            slider.valueProperty().addListener((obs, oldV, newV) -> {
                gainLabel.setText(String.format("%.1f dB", newV.doubleValue()));
            });

            sliders[i] = slider;
            bandBox.getChildren().addAll(gainLabel, slider, freqLabel);
            slidersBox.getChildren().add(bandBox);
        }

        getChildren().addAll(title, controls, slidersBox);
    }

    private boolean isApplyingPreset = false;

    private void applyPreset(String name) {
        if (name == null || !PRESETS.containsKey(name)) return;
        
        isApplyingPreset = true;
        double[] gains = PRESETS.get(name);
        for (int i = 0; i < 10; i++) {
            sliders[i].setValue(gains[i]);
            player.setEqualizerGain(i, gains[i]);
        }
        isApplyingPreset = false;
    }
}
