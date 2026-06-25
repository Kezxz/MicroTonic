package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * User-defined equal temperament.
 *
 * N-TET means the octave is divided into N equal steps.
 *
 * Examples:
 * - 12-TET = normal Western equal temperament
 * - 17-TET = octave split into 17 equal parts
 * - 24-TET = quarter-tone equal temperament
 */
public final class NTETStrategy implements TuningStrategy {

    private static final String[] TWELVE_TET_INTERVAL_NAMES = {
        "tonic",
        "m2",
        "M2",
        "m3",
        "M3",
        "P4",
        "tritone",
        "P5",
        "m6",
        "M6",
        "m7",
        "M7"
    };

    @Override
    public String id() {
        return "n-tet";
    }

    @Override
    public String displayName() {
        return "N-TET";
    }

    /**
     * Resolves a note index using the formula:
     *
     * frequency = tonicFrequency * 2^(noteIndex / divisions)
     *
     * The number of divisions comes from TuningContext.
     */
    @Override
    public TunedNote resolve(int noteIndex, TuningContext context) {
        int divisions = context.divisionsPerOctave();

        double frequencyHz = context.tonicFrequencyHz() * Math.pow(2.0, noteIndex / (double) divisions);

        // used as the MIDI anchor for pitch-bending toward the target frequency
        int nearestMidiNote = MusicMath.frequencyToNearestMidiNote(frequencyHz);

        double nearestMidiFrequency = MusicMath.midiNoteToFrequency(nearestMidiNote);
        double centsDeviation = MusicMath.centsBetween(frequencyHz, nearestMidiFrequency);

        return new TunedNote(
                noteIndex,
                frequencyHz,
                nearestMidiNote,
                centsDeviation,
                displayNameFor(noteIndex, divisions)
        );
    }

    private String displayNameFor(int noteIndex, int divisions) {
        int scaleDegree = Math.floorMod(noteIndex, divisions);

        double twelveTetEquivalent = scaleDegree * 12.0 / divisions;
        int nearestTwelveTetDegree = (int) Math.round(twelveTetEquivalent);
        double centsDeviationFromNearestTwelveTet = (twelveTetEquivalent - nearestTwelveTetDegree) * 100.0;

        return String.format(
                "%d-TET Degree %d - near %s (%+.1f cents)",
                divisions,
                scaleDegree + 1,
                nearestTwelveTetIntervalName(noteIndex, divisions),
                centsDeviationFromNearestTwelveTet
        );
    }

    private String nearestTwelveTetIntervalName(int noteIndex, int divisions) {
        int nTetDegree = Math.floorMod(noteIndex, divisions);
        double twelveTetEquivalent = nTetDegree * 12.0 / divisions;
        int nearestTwelveTetDegree = Math.floorMod((int) Math.round(twelveTetEquivalent), 12);

        return TWELVE_TET_INTERVAL_NAMES[nearestTwelveTetDegree];
    }
}