package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.input.InputMode;
import com.kezxz.microtonic.input.KeyboardLayout;
import com.kezxz.microtonic.input.MidiDeviceService;
import com.kezxz.microtonic.input.MidiInputProvider;
import com.kezxz.microtonic.sound.SoundEngine;
import com.kezxz.microtonic.sound.SoundEngineFactory;
import com.kezxz.microtonic.sound.SoundSource;
import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningEngine;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

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
    private final MidiSetupPane midiSetupPane;

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
        this.midiSetupPane = new MidiSetupPane(
                midiDeviceService,
                this::connectMidiDevice,
                this::disconnectMidiDevice
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
        TitledPane liveFeedbackPane = currentNotePane.build();
        TitledPane midiDevicesPane = midiSetupPane.build();
        TitledPane utilityPane = this.utilityPane.build();
        TitledPane debugPane = advancedTuningDebugPane.build();

        Label statusLabel = new Label("Try out some different intonations. Play notes to see the live feedback!");
        statusLabel.getStyleClass().add("status-label");

        // builds the vertical layout of the app
        VBox content = new VBox(
                16,
                title,
                subtitle,
                controlsPane,
                liveFeedbackPane,
                midiDevicesPane,
                utilityPane,
                debugPane,
                statusLabel
        );
        content.setPadding(new Insets(20));
        content.getStyleClass().add("app-root");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // keeps the content at least as tall as the visible ScrollPane area
        content.minHeightProperty().bind(scrollPane.viewportBoundsProperty().map(bounds -> bounds.getHeight()));

        return scrollPane;
    }

// ----------- INPUT HANDLING ----------- //

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

// ----------- INPUT AND SOUND SELECTION ------------ //

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

// ----------- PLAYBACK / MIDI DEVICE ACTIONS ----------- //

    private void updateLiveFeedback(String source, int noteIndex, TunedNote tunedNote) {
        currentNotePane.update(source, noteIndex, tunedNote);
    }

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

    private void connectMidiDevice(String selectedDevice) {
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
    }

// ----------- LIFECYCLE ----------- //

    @Override
    public void close() {
        disconnectMidiDevice();
        midiSoundEngine.close();
        waveformSoundEngine.close();
    }
}