package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * 12-tone Pythagorean tuning strategy.
 *
 * Pythagorean tuning is built mostly from pure perfect fifths with the ratio 3/2.
 *
 * This MVP version uses a fixed 12-tone chromatic ratio table ordered from the
 * selected tonic.
 *
 * Important:
 * - This gives us immediate chromatic playing.
 * - Enharmonic spellings are simplified for now.
 * - Later, we can add spelling-aware Pythagorean variants.
 */
public final class PythagoreanStrategy implements TuningStrategy {

    /**
     * Pythagorean chromatic ratio table ordered by chromatic scale degree.
     *
     * Scale degree:
     * 0  = tonic
     * 1  = minor second
     * 2  = major second
     * 3  = minor third
     * 4  = major third
     * 5  = perfect fourth
     * 6  = tritone
     * 7  = perfect fifth
     * 8  = minor sixth
     * 9  = major sixth
     * 10 = minor seventh
     * 11 = major seventh
     */
    private static final double[] RATIOS = {
            1.0 / 1.0,
            2187.0 / 2048.0,
            9.0 / 8.0,
            19683.0 / 16384.0,
            81.0 / 64.0,
            4.0 / 3.0,
            729.0 / 512.0,
            3.0 / 2.0,
            6561.0 / 4096.0,
            27.0 / 16.0,
            59049.0 / 32768.0,
            243.0 / 128.0
    };

    private static final String[] DEGREE_NAMES = {
            "1/1 tonic",
            "2187/2048 minor second",
            "9/8 major second",
            "19683/16384 minor third",
            "81/64 major third",
            "4/3 perfect fourth",
            "729/512 tritone",
            "3/2 perfect fifth",
            "6561/4096 minor sixth",
            "27/16 major sixth",
            "59049/32768 minor seventh",
            "243/128 major seventh"
    };

    @Override
    public String id() {
        return "pythagorean";
    }

    @Override
    public String displayName() {
        return "Pythagorean";
    }

    /**
     * Resolves a note index using the Pythagorean ratio table.
     *
     * Examples:
     * - noteIndex 0  = tonic ratio 1/1
     * - noteIndex 2  = major second ratio 9/8
     * - noteIndex 7  = perfect fifth ratio 3/2
     * - noteIndex 12 = next octave tonic ratio 2/1
     *
     * floorDiv and floorMod make negative note indices behave musically.
     */
    @Override
    public TunedNote resolve(int noteIndex, TuningContext context) {
        int octaveOffset = Math.floorDiv(noteIndex, RATIOS.length);
        int scaleDegree = Math.floorMod(noteIndex, RATIOS.length);

        double octaveMultiplier = Math.pow(2.0, octaveOffset);
        double ratio = RATIOS[scaleDegree] * octaveMultiplier;
        double frequencyHz = context.tonicFrequencyHz() * ratio;

        int nearestMidiNote = MusicMath.frequencyToNearestMidiNote(frequencyHz);
        double nearestMidiFrequency = MusicMath.midiNoteToFrequency(nearestMidiNote);
        double centsDeviation = MusicMath.centsBetween(frequencyHz, nearestMidiFrequency);

        return new TunedNote(
                noteIndex,
                frequencyHz,
                nearestMidiNote,
                centsDeviation,
                displayNameFor(noteIndex, scaleDegree, octaveOffset)
        );
    }

    private String displayNameFor(int noteIndex, int scaleDegree, int octaveOffset) {
        return "Pythagorean scale degree "
                + noteIndex
                + " -- "
                + DEGREE_NAMES[scaleDegree]
                + " -- octave "
                + octaveOffset;
    }
}