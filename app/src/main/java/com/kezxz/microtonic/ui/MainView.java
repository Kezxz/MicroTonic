package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.input.KeyboardLayout;
import com.kezxz.microtonic.input.MidiDeviceService;
import com.kezxz.microtonic.input.MidiInputProvider;
import com.kezxz.microtonic.input.InputMode;
import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningEngine;
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
import javafx.scene.control.Label;
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

    private final MainControlsPane mainControlsPane;
    private final CurrentNotePane currentNotePane;
    private final UtilityPane utilityPane;
    private final AdvancedTuningDebugPane advancedTuningDebugPane;

    public MainView(AppState appState) {
        this.appState = appState;
        this.tuningEngine = new TuningEngine(appState);
        this.midiSoundEngine = SoundEngineFactory.createDefault();
        this.waveformSoundEngine = SoundEngineFactory.createWaveform(appState);
        this.midiDeviceService = new MidiDeviceService();
        this.midiInputProvider = new MidiInputProvider();
        this.mainControlsPane = new MainControlsPane(appState);
        this.currentNotePane = new CurrentNotePane();
        this.utilityPane = new UtilityPane(this::panicAllNotesOff);
        this.advancedTuningDebugPane = new AdvancedTuningDebugPane(
            tuningEngine::resolve,
            this::playDebugNote
        );
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

        TitledPane controlsPane = mainControlsPane.build();
        TitledPane debugPane = advancedTuningDebugPane.build();
        TitledPane liveFeedbackPane = currentNotePane.build();
        TitledPane midiDevicesPane = createMidiDevicesPane();
        TitledPane utilityPane = this.utilityPane.build();

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

// ----------- PLAYBACK / MIDI SAFETY ACTIONS ----------- //

    private void playDebugNote(AdvancedTuningDebugPane.DebugNote debugNote) {
        selectedSoundEngine().playTestNote(
            debugNote.noteIndex(),
            debugNote.noteIndex(),
            debugNote.tunedNote()
        );

        updateLiveFeedback("Debug Test Note", debugNote.noteIndex(), debugNote.tunedNote());
    }

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