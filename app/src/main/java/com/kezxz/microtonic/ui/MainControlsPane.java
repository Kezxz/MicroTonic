package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.input.InputMode;
import com.kezxz.microtonic.sound.GeneralMidiInstruments;
import com.kezxz.microtonic.sound.SoundSource;
import com.kezxz.microtonic.sound.Waveform;
import com.kezxz.microtonic.tuning.PitchClass;
import com.kezxz.microtonic.tuning.TuningSystem;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// main app settings shown at the top of the UI
public final class MainControlsPane {

    private final AppState appState;

    public MainControlsPane(AppState appState) {
        this.appState = appState;
    }

    public TitledPane build() {
        ComboBox<String> tuningSystemBox = createTuningSystemBox();
        ComboBox<String> tonicBox = createTonicBox();
        Spinner<Integer> divisionsSpinner = createDivisionsSpinner();
        ComboBox<String> instrumentBox = createInstrumentBox();
        ComboBox<String> soundSourceBox = createSoundSourceBox();
        ComboBox<String> inputModeBox = createInputModeBox();
        ComboBox<String> waveformBox = createWaveformBox();

        Label divisionsLabel = new Label("N-TET Divisions");
        Label instrumentLabel = new Label("Instrument");
        Label waveformLabel = new Label("Waveform");

        bindContextualControlState(
                divisionsLabel,
                divisionsSpinner,
                instrumentLabel,
                instrumentBox,
                waveformLabel,
                waveformBox
        );

        GridPane tuningGrid = createSectionGrid();
        tuningGrid.add(new Label("Tuning System"), 0, 0);
        tuningGrid.add(tuningSystemBox, 1, 0);
        tuningGrid.add(divisionsLabel, 0, 1);
        tuningGrid.add(divisionsSpinner, 1, 1);
        tuningGrid.add(new Label("Tonic"), 0, 2);
        tuningGrid.add(tonicBox, 1, 2);

        GridPane soundGrid = createSectionGrid();
        soundGrid.add(new Label("Sound Source"), 0, 0);
        soundGrid.add(soundSourceBox, 1, 0);
        soundGrid.add(instrumentLabel, 0, 1);
        soundGrid.add(instrumentBox, 1, 1);
        soundGrid.add(waveformLabel, 0, 2);
        soundGrid.add(waveformBox, 1, 2);

        GridPane inputGrid = createSectionGrid();
        inputGrid.add(new Label("Input Mode"), 0, 0);
        inputGrid.add(inputModeBox, 1, 0);

        VBox controlsLayout = new VBox(
                12,
                createSectionTitle("Tuning"),
                tuningGrid,
                createSectionTitle("Sound"),
                soundGrid,
                createSectionTitle("Input"),
                inputGrid
        );
        controlsLayout.setPadding(new Insets(16));

        TitledPane controlsPane = new TitledPane("Main Controls", controlsLayout);
        controlsPane.setCollapsible(false);

        return controlsPane;
    }

    private void bindContextualControlState(
            Label divisionsLabel,
            Spinner<Integer> divisionsSpinner,
            Label instrumentLabel,
            ComboBox<String> instrumentBox,
            Label waveformLabel,
            ComboBox<String> waveformBox
    ) {
        ObservableValue<Boolean> nTetNotSelected = appState.tuningSystemProperty().map(
                tuningSystem -> TuningSystem.fromDisplayName(tuningSystem) != TuningSystem.N_TET
        );

        ObservableValue<Boolean> generalMidiNotSelected = appState.soundSourceProperty().map(
                soundSource -> SoundSource.fromDisplayName(soundSource) != SoundSource.GENERAL_MIDI
        );

        ObservableValue<Boolean> synthWaveformNotSelected = appState.soundSourceProperty().map(
                soundSource -> SoundSource.fromDisplayName(soundSource) != SoundSource.SYNTH_WAVEFORM
        );

        bindDisabledWhen(divisionsLabel, nTetNotSelected);
        bindDisabledWhen(divisionsSpinner, nTetNotSelected);
        bindDisabledWhen(instrumentLabel, generalMidiNotSelected);
        bindDisabledWhen(instrumentBox, generalMidiNotSelected);
        bindDisabledWhen(waveformLabel, synthWaveformNotSelected);
        bindDisabledWhen(waveformBox, synthWaveformNotSelected);
    }

    private GridPane createSectionGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(120);
        labelColumn.setPrefWidth(120);

        ColumnConstraints controlColumn = new ColumnConstraints();
        controlColumn.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(labelColumn, controlColumn);

        return grid;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private void bindDisabledWhen(Node node, ObservableValue<Boolean> disabledWhen) {
        node.disableProperty().bind(disabledWhen);
    }

    private ComboBox<String> createTuningSystemBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(TuningSystem.displayNames());

        box.valueProperty().bindBidirectional(appState.tuningSystemProperty());

        return box;
    }

    private ComboBox<String> createTonicBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(PitchClass.displayNames());

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
        box.getItems().addAll(GeneralMidiInstruments.displayNames());

        box.valueProperty().bindBidirectional(appState.instrumentProperty());

        return box;
    }

    private ComboBox<String> createSoundSourceBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(SoundSource.displayNames());

        box.valueProperty().bindBidirectional(appState.soundSourceProperty());

        return box;
    }

    private ComboBox<String> createInputModeBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(InputMode.displayNames());

        box.valueProperty().bindBidirectional(appState.inputModeProperty());

        return box;
    }

    private ComboBox<String> createWaveformBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(Waveform.displayNames());

        box.valueProperty().bindBidirectional(appState.waveformProperty());

        return box;
    }
}