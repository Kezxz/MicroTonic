package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.tuning.TunedNote;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

// shows the most recently played or resolved note
public final class CurrentNotePane {

    private final Label sourceLabel = new Label("Source: —");
    private final Label noteIndexLabel = new Label("Note Index: —");
    private final Label frequencyLabel = new Label("Frequency: —");
    private final Label midiLabel = new Label("Nearest MIDI Note: —");
    private final Label centsLabel = new Label("Cents Deviation: —");
    private final Label nameLabel = new Label("Name: —");

    public TitledPane build() {
        GridPane feedbackGrid = new GridPane();
        feedbackGrid.setHgap(12);
        feedbackGrid.setVgap(8);
        feedbackGrid.setPadding(new Insets(16));

        feedbackGrid.add(sourceLabel, 0, 0);
        feedbackGrid.add(noteIndexLabel, 0, 1);
        feedbackGrid.add(frequencyLabel, 0, 2);
        feedbackGrid.add(midiLabel, 0, 3);
        feedbackGrid.add(centsLabel, 0, 4);
        feedbackGrid.add(nameLabel, 0, 5);

        TitledPane feedbackPane = new TitledPane("Current Note", feedbackGrid);
        feedbackPane.setCollapsible(false);

        return feedbackPane;
    }

    public void update(String source, int noteIndex, TunedNote tunedNote) {
        Runnable update = () -> {
            sourceLabel.setText("Source: " + source);
            noteIndexLabel.setText("Note Index: " + noteIndex);
            frequencyLabel.setText(String.format("Frequency: %.3f Hz", tunedNote.frequencyHz()));
            midiLabel.setText("Nearest MIDI Note: " + tunedNote.nearestMidiNote());
            centsLabel.setText(String.format(
                    "Cents Deviation: %.3f",
                    tunedNote.centsDeviationFromNearest12Tet()
            ));
            nameLabel.setText("Name: " + tunedNote.displayName());
        };

        if (Platform.isFxApplicationThread()) {
            update.run();
        } else {
            Platform.runLater(update);
        }
    }
}