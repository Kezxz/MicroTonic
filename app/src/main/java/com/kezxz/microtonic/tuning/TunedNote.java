package com.kezxz.microtonic.tuning;

/**
 * Result of resolving a note through a tuning strategy.
 *
 * This object represents everything the sound engine and UI need to know about a tuned note.
 */
public record TunedNote(
        int noteIndex, // logical input step, such as keyboard index or MIDI-note offset

        double frequencyHz, // final target frequency in Hz

        int nearestMidiNote, // nearest norma 12-TET MIDI note

        // Difference between frequencyHz and nearestMidiNote in cents.
        // Useful for real-time feedback and pitch-bend calculation.
        double centsDeviationFromNearest12Tet,

        String displayName // readable label for UI/debugging
) {
    /**
     * Validates the resolved note.
     */
    public TunedNote {
        if (frequencyHz <= 0.0) {
            throw new IllegalArgumentException("Frequency must be positive.");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }
    }
}