package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.app.AppState;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Builds the main application screen.
 *
 * For now, this is a simple JavaFX view bound directly to AppState.
 * Later, if the app grows, this can evolve into a fuller ViewModel pattern.
 */
public final class MainView {

    private final AppState appState;

    public MainView(AppState appState) {
        this.appState = appState;
    }

    public Parent build() {
        Label title = new Label("MicroTonic");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Microtonal tuning sketchpad");
        subtitle.getStyleClass().add("app-subtitle");

        ComboBox<String> tuningSystemBox = createTuningSystemBox();
        ComboBox<String> tonicBox = createTonicBox();
        Spinner<Integer> divisionsSpinner = createDivisionsSpinner();
        ComboBox<String> instrumentBox = createInstrumentBox();
        ComboBox<String> inputModeBox = createInputModeBox();
        ComboBox<String> waveformBox = createWaveformBox();

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(12);
        controlsGrid.setVgap(12);
        controlsGrid.setPadding(new Insets(16));

        controlsGrid.add(new Label("Tuning System"), 0, 0);
        controlsGrid.add(tuningSystemBox, 1, 0);

        controlsGrid.add(new Label("Tonic"), 0, 1);
        controlsGrid.add(tonicBox, 1, 1);

        controlsGrid.add(new Label("N-TET Divisions"), 0, 2);
        controlsGrid.add(divisionsSpinner, 1, 2);

        controlsGrid.add(new Label("Instrument"), 0, 3);
        controlsGrid.add(instrumentBox, 1, 3);

        controlsGrid.add(new Label("Input Mode"), 0, 4);
        controlsGrid.add(inputModeBox, 1, 4);

        controlsGrid.add(new Label("Waveform"), 0, 5);
        controlsGrid.add(waveformBox, 1, 5);

        TitledPane controlsPane = new TitledPane("Controls", controlsGrid);
        controlsPane.setCollapsible(false);

        Label statusLabel = new Label("Ready. App state is connected; tuning engine comes next.");
        statusLabel.getStyleClass().add("status-label");

        VBox root = new VBox(16, title, subtitle, controlsPane, statusLabel);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("app-root");

        return root;
    }

    private ComboBox<String> createTuningSystemBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(
                "12-TET",
                "N-TET"
                "Just Intonation - 12-tone chromatic",
                "Pythagorean",
                "Meantone",
        );
        box.valueProperty().bindBidirectional(appState.tuningSystemProperty());
        return box;
    }

    private ComboBox<String> createTonicBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(
                "C", "C#/Db", "D", "D#/Eb", "E", "F",
                "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"
        );
        box.valueProperty().bindBidirectional(appState.tonicProperty());
        return box;
    }

    private Spinner<Integer> createDivisionsSpinner() {
        Spinner<Integer> spinner = new Spinner<>(2, 72, appState.getNTetDivisions());
        spinner.setEditable(true);

        appState.nTetDivisionsProperty().bind(spinner.valueProperty());

        return spinner;
    }

    private ComboBox<String> createInstrumentBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(
                "Acoustic Grand Piano",
                "Vibraphone",
                "Violin",
                "Alto Sax"
        );
        box.valueProperty().bindBidirectional(appState.instrumentProperty());
        return box;
    }

    private ComboBox<String> createInputModeBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll("MIDI", "Computer Keyboard");
        box.valueProperty().bindBidirectional(appState.inputModeProperty());
        return box;
    }

    private ComboBox<String> createWaveformBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll("Sine", "Square", "Saw", "Triangle");
        box.valueProperty().bindBidirectional(appState.waveformProperty());
        return box;
    }
}