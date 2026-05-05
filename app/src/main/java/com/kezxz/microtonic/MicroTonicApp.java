package com.kezxz.microtonic;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class MicroTonicApp extends Application {
    
    @Override
    public void start(Stage stage) {
        Label title = new Label("MicroTonic");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Microtonal tuning sketchpad");
        subtitle.getStyleClass().add("app-subtitle");

        ComboBox<String> tuningSystemBox = new ComboBox<>();
        tuningSystemBox.getItems().addAll(
                "12-TET",
                "N-TET",
                "Just Intonation",
                "Pythagorean",
                "Meantone"
        );
        tuningSystemBox.setValue("12-TET");

        ComboBox<String> tonicBox = new ComboBox<>();
        tonicBox.getItems().addAll(
            "C", "C♯/D♭", "D", "D♯/E♭", "E", "F", 
            "F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B"
        );
        tonicBox.setValue("C");

        Spinner<Integer> divisionSpinner = new Spinner<>(2, 72, 12);
        divisionSpinner.setEditable(true);

        ComboBox<String> instrumentBox = new ComboBox<>();
        instrumentBox.getItems().addAll(
            "Acoustic Piano",
            "Vibraphone",
            "Violin",
            "Alto Sax"
        );
        instrumentBox.setValue("Acoustic Piano");

        ComboBox<String> inputModeBox = new ComboBox<>();
        inputModeBox.getItems().addAll("MIDI", "Computer Keyboard");
        inputModeBox.setValue("Computer Keyboard");

        ComboBox<String> waveformBox = new ComboBox<>();
        waveformBox.getItems().addAll("Sine", "Square", "Sawtooth", "Triangle");
        waveformBox.setValue("Sine");

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(12);
        controlsGrid.setVgap(12);
        controlsGrid.setPadding(new Insets(16));

        controlsGrid.add(new Label("Tuning System:"), 0, 0);
        controlsGrid.add(tuningSystemBox, 1, 0);

        controlsGrid.add(new Label("Tonic:"), 0, 1);
        controlsGrid.add(tonicBox, 1, 1);

        controlsGrid.add(new Label("Divisions:"), 0, 2);
        controlsGrid.add(divisionSpinner, 1, 2);

        controlsGrid.add(new Label("Instrument:"), 0, 3);
        controlsGrid.add(instrumentBox, 1, 3);

        controlsGrid.add(new Label("Input Mode:"), 0, 4);
        controlsGrid.add(inputModeBox, 1, 4);

        controlsGrid.add(new Label("Waveform:"), 0, 5);
        controlsGrid.add(waveformBox, 1, 5);

        TitledPane controlsPane = new TitledPane("Controls", controlsGrid);
        controlsPane.setCollapsible(false);

        Label statusLabel = new Label("Ready. Audio, MIDI, and tuning engine will be added next.");
        statusLabel.getStyleClass().add("status-label");

        VBox root = new VBox(16, title, subtitle, controlsPane, statusLabel);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("app-root");

        Scene scene = new Scene(root, 560, 430);
        scene.getStylesheets().add(MicroTonicApp.class.getResource("/com/kezxz/microtonic/styles.css").toExternalForm());

        stage.setTitle("MicroTonic");
        stage.setScene(scene);
        stage.setMinWidth(250);
        stage.setMinHeight(400);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}