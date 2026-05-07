package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * 12-tone chromatic Just Intonation strategy.
 *
 * This uses a practical 5-limit chromatic ratio table.
 *
 * Important:
 * - This is not the only possible chromatic JI tuning.
 * - It is a good MVP starting point because it gives all 12 chromatic steps.
 * - The tonic is always treated as 1/1.
 */
public final class JustIntonationChromaticStrategy implements TuningStrategy {

    /**
     * Ratio table ordered by chromatic scale degree from the tonic.
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
            16.0 / 15.0,
            9.0 / 8.0,
            6.0 / 5.0,
            5.0 / 4.0,
            4.0 / 3.0,
            45.0 / 32.0,
            3.0 / 2.0,
            8.0 / 5.0,
            5.0 / 3.0,
            9.0 / 5.0,
            15.0 / 8.0
    };

    private static final String[] DEGREE_NAMES = {
            "1/1 tonic",
            "16/15 minor second",
            "9/8 major second",
            "6/5 minor third",
            "5/4 major third",
            "4/3 perfect fourth",
            "45/32 tritone",
            "3/2 perfect fifth",
            "8/5 minor sixth",
            "5/3 major sixth",
            "9/5 minor seventh",
            "15/8 major seventh"
    };

    @Override
    public String id() {
        return "ji-12-chromatic";
    }

    @Override
    public String displayName() {
        return "Just Intonation - 12-tone chromatic";
    }

    /**
     * Resolves a note index using the chromatic JI ratio table.
     *
     * Examples:
     * - noteIndex 0  = tonic ratio 1/1
     * - noteIndex 4  = major third ratio 5/4
     * - noteIndex 7  = perfect fifth ratio 3/2
     * - noteIndex 12 = next octave tonic ratio 2/1
     *
     * floorDiv/floorMod are used so negative note indices work correctly too.
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
        return "JI step "
                + noteIndex
                + " - "
                + DEGREE_NAMES[scaleDegree]
                + " octave "
                + octaveOffset;
    }
}