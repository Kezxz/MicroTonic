package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * 12-tone chromatic Just Intonation strategy
 *
 * this uses a practical 5-limit chromatic ratio table
 */
public final class JustIntonationChromaticStrategy implements TuningStrategy {

    // ratio table ordered by chromatic scale degree from the tonic
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

    @Override
    public TunedNote resolve(int noteIndex, TuningContext context) {
        // keeps negative note indices aligned to musical octaves
        int octaveOffset = Math.floorDiv(noteIndex, RATIOS.length);
        int scaleDegree = Math.floorMod(noteIndex, RATIOS.length);

        double octaveMultiplier = Math.pow(2.0, octaveOffset);
        double ratio = RATIOS[scaleDegree] * octaveMultiplier;
        double frequencyHz = context.tonicFrequencyHz() * ratio;

        // uses MIDI anchor for pitch-bending toward target frequency
        int nearestMidiNote = MusicMath.frequencyToNearestMidiNote(frequencyHz);
        double nearestMidiFrequency = MusicMath.midiNoteToFrequency(nearestMidiNote);
        double centsDeviation = MusicMath.centsBetween(frequencyHz, nearestMidiFrequency);

        return new TunedNote(
                noteIndex,
                frequencyHz,
                nearestMidiNote,
                centsDeviation,
                displayNameFor(scaleDegree, octaveOffset)
        );
    }

    private String displayNameFor(int scaleDegree, int octaveOffset) {
        int displayDegree = scaleDegree + 1;

        return String.format(
                "Just Intonation degree %d - %s - octave %d",
                displayDegree,
                DEGREE_NAMES[scaleDegree],
                (octaveOffset + 1)
        );
    }
}