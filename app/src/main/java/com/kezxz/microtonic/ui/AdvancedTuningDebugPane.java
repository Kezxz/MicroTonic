package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.tuning.TunedNote;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.util.function.Consumer;
import java.util.function.IntFunction;

// debug tools for manually resolving and previewing tuned notes
public final class AdvancedTuningDebugPane {

    private final IntFunction<TunedNote> resolveNote;
    private final Consumer<DebugNote> playNote;

    public AdvancedTuningDebugPane(
            IntFunction<TunedNote> resolveNote,
            Consumer<DebugNote> playNote
    ) {
        this.resolveNote = resolveNote;
        this.playNote = playNote;
    }

    public TitledPane build() {
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
            TunedNote tunedNote = resolveNote.apply(noteIndex);

            updateLabels(frequencyLabel, midiLabel, centsLabel, nameLabel, tunedNote);
        });

        playButton.setOnAction(event -> {
            int noteIndex = noteIndexSpinner.getValue();
            TunedNote tunedNote = resolveNote.apply(noteIndex);

            updateLabels(frequencyLabel, midiLabel, centsLabel, nameLabel, tunedNote);
            playNote.accept(new DebugNote(noteIndex, tunedNote));
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

    private void updateLabels(
            Label frequencyLabel,
            Label midiLabel,
            Label centsLabel,
            Label nameLabel,
            TunedNote tunedNote
    ) {
        frequencyLabel.setText(String.format("Frequency: %.3f Hz", tunedNote.frequencyHz()));
        midiLabel.setText("Nearest MIDI Note: " + tunedNote.nearestMidiNote());
        centsLabel.setText(String.format(
                "Cents Deviation: %.3f",
                tunedNote.centsDeviationFromNearest12Tet()
        ));
        nameLabel.setText("Name: " + tunedNote.displayName());
    }

    public record DebugNote(int noteIndex, TunedNote tunedNote) {
    }
}