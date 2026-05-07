package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * 12-tone quarter-comma meantone tuning strategy.
 *
 * Meantone temperament narrows fifths slightly so that major thirds are closer
 * to pure Just Intonation thirds.
 *
 * This MVP version uses a fixed 12-tone cents table ordered from the selected
 * tonic. That keeps the implementation simple and easy to test.
 *
 * Important:
 * - This is a practical MVP meantone map.
 * - Enharmonic spellings are simplified for now.
 * - Later, we can add configurable comma sizes or spelling-aware variants.
 */
public final class MeantoneStrategy implements TuningStrategy {

    /**
     * Quarter-comma meantone cents table ordered by chromatic scale degree.
     *
     * These values are cents above the tonic within one octave.
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
    private static final double[] CENTS = {
            0.000,
            76.049,
            193.157,
            310.265,
            386.314,
            503.422,
            579.471,
            696.578,
            772.627,
            889.735,
            1006.843,
            1082.892
    };

    private static final String[] DEGREE_NAMES = {
            "tonic",
            "minor second",
            "major second",
            "minor third",
            "major third",
            "perfect fourth",
            "tritone",
            "perfect fifth",
            "minor sixth",
            "major sixth",
            "minor seventh",
            "major seventh"
    };

    @Override
    public String id() {
        return "meantone";
    }

    @Override
    public String displayName() {
        return "Meantone";
    }

    /**
     * Resolves a note index using the quarter-comma meantone cents table.
     *
     * Examples:
     * - noteIndex 0  = tonic, 0 cents
     * - noteIndex 4  = major third, about 386.314 cents
     * - noteIndex 7  = perfect fifth, about 696.578 cents
     * - noteIndex 12 = next octave tonic, 1200 cents
     *
     * floorDiv and floorMod make negative note indices behave musically.
     */
    @Override
    public TunedNote resolve(int noteIndex, TuningContext context) {
        int octaveOffset = Math.floorDiv(noteIndex, CENTS.length);
        int scaleDegree = Math.floorMod(noteIndex, CENTS.length);

        double centsFromTonic = CENTS[scaleDegree] + (octaveOffset * 1200.0);
        double ratio = MusicMath.centsToRatio(centsFromTonic);
        double frequencyHz = context.tonicFrequencyHz() * ratio;

        int nearestMidiNote = MusicMath.frequencyToNearestMidiNote(frequencyHz);
        double nearestMidiFrequency = MusicMath.midiNoteToFrequency(nearestMidiNote);
        double centsDeviation = MusicMath.centsBetween(frequencyHz, nearestMidiFrequency);

        return new TunedNote(
                noteIndex,
                frequencyHz,
                nearestMidiNote,
                centsDeviation,
                displayNameFor(noteIndex, scaleDegree, octaveOffset, centsFromTonic)
        );
    }

    private String displayNameFor(
            int noteIndex,
            int scaleDegree,
            int octaveOffset,
            double centsFromTonic
    ) {
        return "Meantone scale degree "
                + noteIndex
                + " -- "
                + DEGREE_NAMES[scaleDegree]
                + " -- octave "
                + octaveOffset
                + " ("
                + String.format("%.3f", centsFromTonic)
                + " cents above tonic)";
    }
}