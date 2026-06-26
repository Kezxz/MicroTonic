package com.kezxz.microtonic.tuning;

// result of resolving a note through a tuning strategy
public record TunedNote(
        int noteIndex, // logical input step, such as keyboard index or MIDI-note offset
        double frequencyHz, // final target frequency in Hz
        int nearestMidiNote,
        double centsDeviationFromNearest12Tet,
        String displayName
) {

    public TunedNote {
        if (frequencyHz <= 0.0) {
            throw new IllegalArgumentException("Frequency must be positive.");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }
    }
}