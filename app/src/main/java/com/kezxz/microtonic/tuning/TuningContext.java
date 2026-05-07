package com.kezxz.microtonic.tuning;

import com.kezxz.microtonic.util.MusicMath;

/**
 * Input settings used when resolving a note through a tuning strategy.
 *
 * This object answers questions like:
 * - What is the tonic?
 * - What frequency is the tonic?
 * - How many divisions per octave should N-TET use?
 */
public record TuningContext(
        PitchClass tonic,
        double tonicFrequencyHz,
        int divisionsPerOctave
) {
    /**
     * Default musical context:
     * - tonic C
     * - C4 as the root frequency
     * - 12 divisions per octave
     */
    public static TuningContext defaultContext() {
        return new TuningContext(PitchClass.C, MusicMath.C4_FREQUENCY_HZ, 12);
    }

    /**
     * Compact constructor for validation.
     *
     * In Java records, this runs automatically whenever a new TuningContext is created.
     */
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