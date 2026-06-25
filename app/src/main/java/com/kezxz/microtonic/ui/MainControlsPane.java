package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.input.InputMode;
import com.kezxz.microtonic.sound.GeneralMidiInstruments;
import com.kezxz.microtonic.sound.SoundSource;
import com.kezxz.microtonic.sound.Waveform;
import com.kezxz.microtonic.tuning.PitchClass;
import com.kezxz.microtonic.tuning.TuningSystem;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

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

        bindContextualControlState(divisionsSpinner, instrumentBox, waveformBox);

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

        controlsGrid.add(new Label("Sound Source"), 0, 4);
        controlsGrid.add(soundSourceBox, 1, 4);

        controlsGrid.add(new Label("Input Mode"), 0, 5);
        controlsGrid.add(inputModeBox, 1, 5);

        controlsGrid.add(new Label("Waveform"), 0, 6);
        controlsGrid.add(waveformBox, 1, 6);

        TitledPane controlsPane = new TitledPane("Main Controls", controlsGrid);
        controlsPane.setCollapsible(false);

        return controlsPane;
    }

    private void bindContextualControlState(
            Spinner<Integer> divisionsSpinner,
            ComboBox<String> instrumentBox,
            ComboBox<String> waveformBox
    ) {
        divisionsSpinner.disableProperty().bind(
                appState.tuningSystemProperty().map(
                        tuningSystem -> TuningSystem.fromDisplayName(tuningSystem) != TuningSystem.N_TET
                )
        );

        instrumentBox.disableProperty().bind(
                appState.soundSourceProperty().map(
                        soundSource -> SoundSource.fromDisplayName(soundSource) != SoundSource.GENERAL_MIDI
                )
        );

        waveformBox.disableProperty().bind(
                appState.soundSourceProperty().map(
                        soundSource -> SoundSource.fromDisplayName(soundSource) != SoundSource.SYNTH_WAVEFORM
                )
        );
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