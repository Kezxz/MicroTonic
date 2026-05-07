package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * Standard 12-tone equal temperament.
 *
 * This is the familiar modern piano/guitar tuning where each octave is divided
 * into 12 equal steps.
 */
public final class TwelveTETStrategy implements TuningStrategy {

    @Override
    public String id() {
        return "12-tet";
    }

    @Override
    public String displayName() {
        return "12-TET";
    }

    /**
     * Resolves a note index using the formula:
     *
     * frequency = tonicFrequency * 2^(noteIndex / 12)
     *
     * Examples when tonic is C4:
     * - noteIndex 0 = C4
     * - noteIndex 12 = C5
     * - noteIndex -12 = C3
     */
    @Override
    public TunedNote resolve(int noteIndex, TuningContext context) {
        double frequencyHz = context.tonicFrequencyHz() * Math.pow(2.0, noteIndex / 12.0);

        // Find the nearest standard MIDI note. For 12-TET this should usually
        // be exactly the note we expect, with near-zero cents deviation.
        int nearestMidiNote = MusicMath.frequencyToNearestMidiNote(frequencyHz);

        // Convert that MIDI note back to its normal frequency so we can measure
        // how far away the tuned frequency is.
        double nearestMidiFrequency = MusicMath.midiNoteToFrequency(nearestMidiNote);

        // In 12-TET this should be approximately zero, except for tiny floating
        // point rounding differences.
        double centsDeviation = MusicMath.centsBetween(frequencyHz, nearestMidiFrequency);

        return new TunedNote(
                noteIndex,
                frequencyHz,
                nearestMidiNote,
                centsDeviation,
                displayNameFor(noteIndex)
        );
    }

    /**
     * Simple label for debugging and future feedback display.
     */
    private String displayNameFor(int noteIndex) {
        return "12-TET step " + noteIndex;
    }
}