package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.input.KeyboardLayout;
import com.kezxz.microtonic.input.MidiDeviceService;
import com.kezxz.microtonic.input.MidiInputProvider;
import com.kezxz.microtonic.input.InputMode;
import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningEngine;
import com.kezxz.microtonic.tuning.TuningSystem;
import com.kezxz.microtonic.sound.GeneralMidiInstruments;
import com.kezxz.microtonic.sound.Waveform;
import com.kezxz.microtonic.sound.SoundSource;
import com.kezxz.microtonic.sound.SoundEngine;
import com.kezxz.microtonic.sound.SoundEngineFactory;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ListView;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

public final class MainView implements AutoCloseable {

    private final AppState appState;
    private final TuningEngine tuningEngine;
    private final SoundEngine midiSoundEngine;
    private final SoundEngine waveformSoundEngine;
    private final MidiDeviceService midiDeviceService;
    private final MidiInputProvider midiInputProvider;
    private final Set<KeyCode> activeComputerKeys = new HashSet<>();

    private final CurrentNotePane currentNotePane;

    public MainView(AppState appState) {
        this.appState = appState;
        this.tuningEngine = new TuningEngine(appState);
        this.midiSoundEngine = SoundEngineFactory.createDefault();
        this.waveformSoundEngine = SoundEngineFactory.createWaveform(appState);
        this.midiDeviceService = new MidiDeviceService();
        this.midiInputProvider = new MidiInputProvider();
        this.currentNotePane = new CurrentNotePane();
        this.appState.instrumentProperty().addListener((observable, oldValue, newValue) ->
                midiSoundEngine.setInstrumentByName(newValue)
        );
        this.appState.soundSourceProperty().addListener((observable, oldValue, newValue) ->
                panicAllNotesOff()
        );
        this.appState.inputModeProperty().addListener((observable, oldValue, newValue) ->
        panicAllNotesOff()
        );
        this.midiSoundEngine.setInstrumentByName(appState.getInstrument());
    }

// ----------- ROOT VIEW ----------- //

    public Parent build() {
        Label title = new Label("MicroTonic");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Microtonal tuning sketchpad");
        subtitle.getStyleClass().add("app-subtitle");

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

        // groups main controls in a titled section
        TitledPane controlsPane = new TitledPane("Main Controls", controlsGrid);
        controlsPane.setCollapsible(false);

        TitledPane debugPane = createTuningDebugPane();
        TitledPane liveFeedbackPane = currentNotePane.build();
        TitledPane midiDevicesPane = createMidiDevicesPane();
        TitledPane utilityPane = createUtilityPane();

        Label statusLabel = new Label("Try out some different intonations. Play notes to see the live feedback!");
        statusLabel.getStyleClass().add("status-label");

        // builds the vertical layout of the app
        VBox content = new VBox(16, title, subtitle, controlsPane, liveFeedbackPane, midiDevicesPane, utilityPane, debugPane, statusLabel);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("app-root");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // makes the VBox at least as tall as the visible ScrollPane area
        content.minHeightProperty().bind(scrollPane.viewportBoundsProperty().map(bounds -> bounds.getHeight()));

        return scrollPane;
    }

// ----------- MAIN CONTROLS PANE ----------- //

    private ComboBox<String> createTuningSystemBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(TuningSystem.displayNames());

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

    // N-TET divisions....min = 2, max = 72
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

// ----------- CURRENT NOTE / LIVE FEEDBACK UPDATE ----------- //

    private void updateLiveFeedback(String source, int noteIndex, TunedNote tunedNote) {
        currentNotePane.update(source, noteIndex, tunedNote);
    }

// ----------- MIDI SETUP PANE ----------- //

    private TitledPane createMidiDevicesPane() {
        ListView<String> midiDeviceList = new ListView<>();
        midiDeviceList.setPrefHeight(120);

        Button refreshButton = new Button("Refresh MIDI Devices");
        Button connectButton = new Button("Connect Selected MIDI Device");
        Button disconnectButton = new Button("Disconnect MIDI Device");

        Label statusLabel = new Label("Ready. Select a tuning, choose an input source, and play.");

        Runnable refreshDevices = () -> {
            var devices = midiDeviceService.listInputDevices();

            if (devices.isEmpty()) {
                midiDeviceList.setItems(FXCollections.observableArrayList("No MIDI input devices found."));
                statusLabel.setText("No user-facing MIDI controllers found. Connect one, then click Refresh MIDI Devices.");
                return;
            }

            var displayNames = devices.stream()
                    .map(MidiDeviceService.MidiInputDeviceInfo::displayName)
                    .toList();

            midiDeviceList.setItems(FXCollections.observableArrayList(displayNames));
            statusLabel.setText(devices.size() + " MIDI input device(s) found.");
        };

        refreshButton.setOnAction(event -> refreshDevices.run());

        connectButton.setOnAction(event -> {
            String selectedDevice = midiDeviceList.getSelectionModel().getSelectedItem();

            if (selectedDevice == null || selectedDevice.equals("No MIDI input devices found.")) {
                statusLabel.setText("Select a MIDI input device first.");
                return;
            }

            try {
                panicAllNotesOff();
                midiInputProvider.close();

                midiInputProvider.openByDisplayName(
                        selectedDevice,
                        new MidiInputProvider.MidiNoteListener() {
                            @Override
                            public void noteOn(int midiNote, int velocity) {
                                handleMidiNoteOn(midiNote, velocity);
                            }

                            @Override
                            public void noteOff(int midiNote) {
                                handleMidiNoteOff(midiNote);
                            }
                        }
                );

                statusLabel.setText("Connected to " + selectedDevice);
            } catch (RuntimeException exception) {
                statusLabel.setText("Could not connect: " + exception.getMessage());
            }
        });

        disconnectButton.setOnAction(event -> {
            disconnectMidiDevice();
            statusLabel.setText("MIDI device disconnected.");
        });

        refreshDevices.run();

        GridPane midiGrid = new GridPane();
        midiGrid.setHgap(12);
        midiGrid.setVgap(12);
        midiGrid.setPadding(new Insets(16));

        midiGrid.add(refreshButton, 0, 0);
        midiGrid.add(connectButton, 1, 0);
        midiGrid.add(disconnectButton, 2, 0);
        midiGrid.add(statusLabel, 0, 1, 3, 1);
        midiGrid.add(midiDeviceList, 0, 2, 3, 1);

        TitledPane midiDevicesPane = new TitledPane("MIDI Setup", midiGrid);
        midiDevicesPane.setCollapsible(false);

        return midiDevicesPane;
    }

// ----------- UTILITY PANE ---------- //

    private TitledPane createUtilityPane() {
        GridPane utilityGrid = new GridPane();
        utilityGrid.setHgap(12);
        utilityGrid.setVgap(12);
        utilityGrid.setPadding(new Insets(16));

        Button panicButton = createPanicButton();

        Label helpLabel = new Label("Use this if notes get stuck or MIDI behaves unexpectedly.");

        utilityGrid.add(panicButton, 0, 0);
        utilityGrid.add(helpLabel, 1, 0);

        TitledPane utilityPane = new TitledPane("Utilities", utilityGrid);
        utilityPane.setCollapsible(true);
        utilityPane.setExpanded(false);

        return utilityPane;
    }

    private Button createPanicButton() {
        Button panicButton = new Button("Panic / All Notes Off");
        panicButton.setOnAction(event -> panicAllNotesOff());
        return panicButton;
    }

// ----------- ADVANCED DEBUG PANE ----------- //

    private TitledPane createTuningDebugPane() {
        Spinner<Integer> noteIndexSpinner = new Spinner<>(-48, 48, 0);
        noteIndexSpinner.setEditable(true);

        Button resolveButton = new Button("Resolve Note");
        Button playButton = new Button("Play Test Note");

        Label frequencyLabel = new Label("Frequency: —");
        Label midiLabel = new Label("Nearest MIDI Note: —");
        Label centsLabel = new Label("Cents Deviation: —");
        Label nameLabel = new Label("Name: —");

        resolveButton.setOnAction(event -> {
            int noteIndex = noteIndexSpinner.getValue();
            TunedNote tunedNote = tuningEngine.resolve(noteIndex);

            frequencyLabel.setText(String.format("Frequency: %.3f Hz", tunedNote.frequencyHz()));
            midiLabel.setText("Nearest MIDI Note: " + tunedNote.nearestMidiNote());
            centsLabel.setText(String.format(
                    "Cents Deviation: %.3f",
                    tunedNote.centsDeviationFromNearest12Tet()
            ));
            nameLabel.setText("Name: " + tunedNote.displayName());
        });

        playButton.setOnAction(event -> {
            int noteIndex = noteIndexSpinner.getValue();
            TunedNote tunedNote = tuningEngine.resolve(noteIndex);

            frequencyLabel.setText(String.format("Frequency: %.3f Hz", tunedNote.frequencyHz()));
            midiLabel.setText("Nearest MIDI Note: " + tunedNote.nearestMidiNote());
            centsLabel.setText(String.format(
                    "Cents Deviation: %.3f",
                    tunedNote.centsDeviationFromNearest12Tet()
            ));
            nameLabel.setText("Name: " + tunedNote.displayName());

            selectedSoundEngine().playTestNote(noteIndex, noteIndex, tunedNote);
            updateLiveFeedback("Debug Test Note", noteIndex, tunedNote);
        });

        GridPane debugGrid = new GridPane();
        debugGrid.setHgap(12);
        debugGrid.setVgap(12);
        debugGrid.setPadding(new Insets(16));

        debugGrid.add(new Label("Note Index"), 0, 0);
        debugGrid.add(noteIndexSpinner, 1, 0);
        debugGrid.add(resolveButton, 2, 0);
        debugGrid.add(playButton, 3, 0);

        debugGrid.add(frequencyLabel, 0, 1, 4, 1);
        debugGrid.add(midiLabel, 0, 2, 4, 1);
        debugGrid.add(centsLabel, 0, 3, 4, 1);
        debugGrid.add(nameLabel, 0, 4, 4, 1);

        TitledPane debugPane = new TitledPane("Advanced Tuning Debug", debugGrid);
        debugPane.setCollapsible(true);
        debugPane.setExpanded(false);

        return debugPane;
    }


// ----------- INPUT EVENT HANDLERS ----------- //

public void handleKeyPressed(KeyEvent event) {
        if (!isComputerKeyboardInputEnabled()) {
            return;
        }

        if (shouldIgnoreKeyEvent(event)) {
            return;
        }

        KeyCode keyCode = event.getCode();

        if (activeComputerKeys.contains(keyCode)) {
            return;
        }

        OptionalInt noteIndex = KeyboardLayout.noteIndexFor(keyCode);

        if (noteIndex.isEmpty()) {
            return;
        }

        activeComputerKeys.add(keyCode);

        TunedNote tunedNote = tuningEngine.resolve(noteIndex.getAsInt());

        selectedSoundEngine().noteOn(
                keyCode.getCode(),
                noteIndex.getAsInt(),
                tunedNote,
                100
        );

        updateLiveFeedback("Computer Keyboard", noteIndex.getAsInt(), tunedNote);

        event.consume();
    }

    public void handleKeyReleased(KeyEvent event) {
        if (!isComputerKeyboardInputEnabled()) {
            return;
        }

        KeyCode keyCode = event.getCode();
        OptionalInt noteIndex = KeyboardLayout.noteIndexFor(keyCode);

        if (noteIndex.isEmpty()) {
            return;
        }

        activeComputerKeys.remove(keyCode);
        selectedSoundEngine().noteOff(keyCode.getCode());

        event.consume();
    }

    // MIDI note 60 maps to noteIndex 0, controller's 'middle C' plays the selected tonic
    private void handleMidiNoteOn(int midiNote, int velocity) {
        if (!isMidiInputEnabled()) {
            return;
        }
        
        int noteIndex = midiNote - MidiInputProvider.REFERENCE_MIDI_NOTE;
        TunedNote tunedNote = tuningEngine.resolve(noteIndex);

        selectedSoundEngine().noteOn(
                midiNote,
                noteIndex,
                tunedNote,
                velocity
        );

        updateLiveFeedback("MIDI", noteIndex, tunedNote);
    }

    private void handleMidiNoteOff(int midiNote) {
        if (!isMidiInputEnabled()) {
            return;
        }

        selectedSoundEngine().noteOff(midiNote);
    }

    private void bindContextualControlState(
            Spinner<Integer> divisionsSpinner,
            ComboBox<String> instrumentBox,
            ComboBox<String> waveformBox
    ) {
        divisionsSpinner.disableProperty().bind(
                appState.tuningSystemProperty().map(tuningSystem -> !isNtetSelected())
        );

        instrumentBox.disableProperty().bind(
                appState.soundSourceProperty().map(soundSource -> !isGeneralMidiSelected())
        );

        waveformBox.disableProperty().bind(
                appState.soundSourceProperty().map(soundSource -> !isSynthWaveformSelected())
        );
    }

// ----------- INPUT MODE HANDLERS ------------ //

    // avoids playing notes while the user is typing into editable controls
    private boolean shouldIgnoreKeyEvent(KeyEvent event) {
        return event.getTarget() instanceof TextInputControl;
    }

    private boolean isComputerKeyboardInputEnabled() {
        return InputMode.fromDisplayName(appState.getInputMode()) == InputMode.COMPUTER_KEYBOARD;
    }

    private boolean isMidiInputEnabled() {
        return InputMode.fromDisplayName(appState.getInputMode()) == InputMode.MIDI;
    }

    private SoundEngine selectedSoundEngine() {
        if (SoundSource.fromDisplayName(appState.getSoundSource()) == SoundSource.SYNTH_WAVEFORM) {
            return waveformSoundEngine;
        }

    return midiSoundEngine;
    }

// ----------- UI HELPERS ----------- //

    private boolean isNtetSelected() {
        return TuningSystem.fromDisplayName(appState.getTuningSystem()) == TuningSystem.N_TET;
    }

    private boolean isGeneralMidiSelected() {
        return SoundSource.fromDisplayName(appState.getSoundSource()) == SoundSource.GENERAL_MIDI;
    }

    private boolean isSynthWaveformSelected() {
        return SoundSource.fromDisplayName(appState.getSoundSource()) == SoundSource.SYNTH_WAVEFORM;
    }
// ----------- PLAYBACK / MIDI SAFETY ACTIONS ----------- //

    private void panicAllNotesOff() {
        activeComputerKeys.clear();
        midiSoundEngine.allNotesOff();
        waveformSoundEngine.allNotesOff();
    }

    private void disconnectMidiDevice() {
        panicAllNotesOff();
        midiInputProvider.close();
    }

// ----------- LIFECYCLE ----------- //

    @Override
    public void close() {
        disconnectMidiDevice();
        midiSoundEngine.close();
        waveformSoundEngine.close();
    }
}