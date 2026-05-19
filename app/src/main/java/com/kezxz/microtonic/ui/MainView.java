package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningEngine;
import com.kezxz.microtonic.sound.midi.MidiSoundEngine;
import com.kezxz.microtonic.sound.GeneralMidiInstruments;
import com.kezxz.microtonic.input.KeyboardLayout;
import com.kezxz.microtonic.input.MidiDeviceService;
import com.kezxz.microtonic.input.MidiInputProvider;

import javafx.collections.FXCollections;

import javafx.geometry.Insets;

import javafx.scene.Parent;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ListView;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Builds the main application screen.
 * 
 * This class owns the UI layout, but not the app's core logic
 *
 * For now, this is a simple JavaFX view bound directly to AppState.
 * Later, if the app grows, this can evolve into a fuller ViewModel pattern.
 */
public final class MainView implements AutoCloseable {

    private final AppState appState;
    private final TuningEngine tuningEngine;
    private final MidiSoundEngine midiSoundEngine;
    private final MidiDeviceService midiDeviceService;
    private final MidiInputProvider midiInputProvider;
    private final Set<KeyCode> activeComputerKeys = new HashSet<>();

    /**
     * The view needs access to AppState so controls can read and update settings.
     */
    public MainView(AppState appState) {
        this.appState = appState;
        this.tuningEngine = new TuningEngine(appState);
        this.midiSoundEngine = new MidiSoundEngine();
        this.midiDeviceService = new MidiDeviceService();
        this.midiInputProvider = new MidiInputProvider();
        this.appState.instrumentProperty().addListener((observable, oldValue, newValue) ->
                midiSoundEngine.setInstrumentByName(newValue)
        );

        this.midiSoundEngine.setInstrumentByName(appState.getInstrument());
    }

    /**
     * Builds and returns the full JavaFX visual tree for the main screen.
     *
     * Parent is the common base type for JavaFX nodes that can be used as the root of a Scene.
     */
    public Parent build() {
        Label title = new Label("MicroTonic");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Microtonal tuning sketchpad");
        subtitle.getStyleClass().add("app-subtitle");

        // Create each control through a helper method so this build method stays readable.
        ComboBox<String> tuningSystemBox = createTuningSystemBox();
        ComboBox<String> tonicBox = createTonicBox();
        Spinner<Integer> divisionsSpinner = createDivisionsSpinner();
        ComboBox<String> instrumentBox = createInstrumentBox();
        ComboBox<String> inputModeBox = createInputModeBox();
        ComboBox<String> waveformBox = createWaveformBox();

        // GridPane is useful for simple label/control form layouts. Maybe upgrade to something more complex later.
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

        // TitledPane gives the control group a simple labeled container.
        TitledPane controlsPane = new TitledPane("Controls", controlsGrid);
        controlsPane.setCollapsible(false);

        TitledPane debugPane = createTuningDebugPane();
        TitledPane midiDevicesPane = createMidiDevicesPane();

        Label statusLabel = new Label("Tuning engine up and running. Wiring to computer keyboard and/or MIDI input next.");
        statusLabel.getStyleClass().add("status-label");

        // VBox stacks the title, subtitle, controls, and status vertically.
        VBox content = new VBox(16, title, subtitle, controlsPane, debugPane, midiDevicesPane, statusLabel);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("app-root");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // make the VBox at least as tall as the visible ScrollPane area
        content.minHeightProperty().bind(scrollPane.viewportBoundsProperty().map(bounds -> bounds.getHeight()));

        return scrollPane;
    }

    /**
     * Handles MIDI note-on events from the connected MIDI controller.
     *
     * MIDI note 60 maps to noteIndex 0.
     * This means the controller's middle C plays the selected tonic.
     */
    private void handleMidiNoteOn(int midiNote, int velocity) {
        int noteIndex = midiNote - MidiInputProvider.REFERENCE_MIDI_NOTE;
        TunedNote tunedNote = tuningEngine.resolve(noteIndex);

        midiSoundEngine.noteOn(
                midiNote,
                noteIndex,
                tunedNote,
                velocity
        );
    }

    /**
     * Handles MIDI note-off events from the connected MIDI controller.
     */
    private void handleMidiNoteOff(int midiNote) {
        midiSoundEngine.noteOff(midiNote);
    }

    /**
     * Handles computer keyboard note-on events.
     */
    public void handleKeyPressed(KeyEvent event) {
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

        midiSoundEngine.noteOn(
                keyCode.getCode(),
                noteIndex.getAsInt(),
                tunedNote,
                100
        );

        event.consume();
    }

    /**
     * Handles computer keyboard note-off events.
     *
     * The same key code used for note-on is used as the input note ID for note-off.
     */
    public void handleKeyReleased(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        OptionalInt noteIndex = KeyboardLayout.noteIndexFor(keyCode);

        if (noteIndex.isEmpty()) {
            return;
        }

        activeComputerKeys.remove(keyCode);
        midiSoundEngine.noteOff(keyCode.getCode());

        event.consume();
    }

    /**
     * Avoids playing notes while the user is typing into editable controls.
     *
     * This matters because the N-TET spinner has a text editor.
     */
    private boolean shouldIgnoreKeyEvent(KeyEvent event) {
        return event.getTarget() instanceof TextInputControl;
    }

    /**
     * Creates a temporary MIDI device listing panel.
     */
    private TitledPane createMidiDevicesPane() {
        ListView<String> midiDeviceList = new ListView<>();
        midiDeviceList.setPrefHeight(120);

        Button refreshButton = new Button("Refresh MIDI Devices");
        Button connectButton = new Button("Connect Selected MIDI Device");

        Label statusLabel = new Label();

        Runnable refreshDevices = () -> {
            var devices = midiDeviceService.listInputDevices();

            if (devices.isEmpty()) {
                midiDeviceList.setItems(FXCollections.observableArrayList("No MIDI input devices found."));
                statusLabel.setText("Connect a MIDI controller, then click Refresh MIDI Devices.");
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

        refreshDevices.run();

        GridPane midiGrid = new GridPane();
        midiGrid.setHgap(12);
        midiGrid.setVgap(12);
        midiGrid.setPadding(new Insets(16));

        midiGrid.add(refreshButton, 0, 0);
        midiGrid.add(connectButton, 1, 0);
        midiGrid.add(statusLabel, 0, 1, 2, 1);
        midiGrid.add(midiDeviceList, 0, 2, 2, 1);

        TitledPane midiDevicesPane = new TitledPane("MIDI Devices", midiGrid);
        midiDevicesPane.setCollapsible(false);

        return midiDevicesPane;
    }

    /**
     * Creates a small debug panel for manually testing tuning results.
     * Later, this same information can move into the real-time feedback panel.
     */
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

            midiSoundEngine.playTestNote(noteIndex, noteIndex, tunedNote);
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

        TitledPane debugPane = new TitledPane("Tuning Debug", debugGrid);
        debugPane.setCollapsible(false);

        return debugPane;
    }

    /**
     * Creates the tuning-system dropdown and binds it to AppState.
     */
    private ComboBox<String> createTuningSystemBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(
                "12-TET",
                "N-TET",
                "Just Intonation - 12-tone chromatic",
                "Pythagorean",
                "Meantone"
        );

        box.valueProperty().bindBidirectional(appState.tuningSystemProperty());
        return box;
    }

    /**
     * Creates the tonic/root-note dropdown.
     */
    private ComboBox<String> createTonicBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(
                "C", "C#/Db", "D", "D#/Eb", "E", "F",
                "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"
        );
        box.valueProperty().bindBidirectional(appState.tonicProperty());
        return box;
    }

    /**
     * Creates the N-TET division spinner.
     *
     * MVP range:
     * - minimum 2 divisions per octave
     * - maximum 72 divisions per octave
     *
     * Examples:
     * - 12 = normal 12-TET
     * - 17 = 17-TET
     * - 24 = quarter-tone equal temperament
     */
    private Spinner<Integer> createDivisionsSpinner() {
        Spinner<Integer> spinner = new Spinner<>(2, 72, appState.getNTetDivisions());
        spinner.setEditable(true);

        appState.nTetDivisionsProperty().bind(spinner.valueProperty());

        return spinner;
    }

    /**
     * Creates the General MIDI instrument dropdown.
     */
    private ComboBox<String> createInstrumentBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(GeneralMidiInstruments.displayNames());

        box.valueProperty().bindBidirectional(appState.instrumentProperty());

        return box;
    }

    /**
     * Creates the input mode dropdown.
     */
    private ComboBox<String> createInputModeBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll("MIDI", "Computer Keyboard");
        box.valueProperty().bindBidirectional(appState.inputModeProperty());
        return box;
    }

    /**
     * Creates the waveform dropdown for the future synth engine.
     */
    private ComboBox<String> createWaveformBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll("Sine", "Square", "Saw", "Triangle");
        box.valueProperty().bindBidirectional(appState.waveformProperty());
        return box;
    }

    @Override
    public void close() {
        activeComputerKeys.clear();
        midiInputProvider.close();
        midiSoundEngine.close();
    }
}