package com.kezxz.microtonic.tuning;

import com.kezxz.microtonic.util.MusicMath;

// settings used when resolving a note through a tuned note
public record TuningContext(
        PitchClass tonic,
        double tonicFrequencyHz,
        int divisionsPerOctave
) {
    // default context: C4 root, 12 divisions per octave
    public static TuningContext defaultContext() {
        return new TuningContext(PitchClass.C, MusicMath.C4_FREQUENCY_HZ, 12);
    }

    public TuningContext {
        if (tonic == null) {
            throw new IllegalArgumentException("Tonic cannot be null.");
        }

        if (tonicFrequencyHz <= 0.0) {
            throw new IllegalArgumentException("Tonic frequency must be positive.");
        }

        if (divisionsPerOctave < 2) {
            throw new IllegalArgumentException("Divisions per octave must be at least 2.");
        }
    }
}