package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * 12-tone quarter-comma meantone tuning strategy
 *
 * meantone temperament narrows fifths slightly so that major thirds are closer
 * to pure Just Intonation thirds
 */
public final class MeantoneStrategy implements TuningStrategy {

    // cents above tonic
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

    @Override
    public TunedNote resolve(int noteIndex, TuningContext context) {
        // keeps negative indices aligned to musical octaves
        int octaveOffset = Math.floorDiv(noteIndex, CENTS.length);
        int scaleDegree = Math.floorMod(noteIndex, CENTS.length);

        double centsFromTonic = CENTS[scaleDegree] + (octaveOffset * 1200.0);
        double ratio = MusicMath.centsToRatio(centsFromTonic);
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
                displayNameFor(scaleDegree, octaveOffset, centsFromTonic)
        );
    }

    private String displayNameFor(
            int scaleDegree,
            int octaveOffset,
            double centsFromTonic
    ) {
        int displayDegree = scaleDegree + 1;

        return String.format(
                "Meantone degree %d - %s - octave %d - %.3f cents",
                displayDegree,
                DEGREE_NAMES[scaleDegree],
                (octaveOffset + 1),
                centsFromTonic
        );
    }
}