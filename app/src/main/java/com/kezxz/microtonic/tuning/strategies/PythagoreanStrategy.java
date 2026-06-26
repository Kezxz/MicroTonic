package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * 12-tone Pythagorean tuning strategy
 *
 * Pythagorean tuning is built mostly from pure perfect fifths with the ratio 3/2
 */
public final class PythagoreanStrategy implements TuningStrategy {

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
                "Pythagorean Intonation degree %d - %s - octave %d",
                displayDegree,
                DEGREE_NAMES[scaleDegree],
                (octaveOffset + 1)
        );
    }
}