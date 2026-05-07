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

        // Even though N-TET may not line up with standard MIDI notes, we still
        // find the nearest MIDI note so the future MIDI sound engine can pitch
        // bend from that note to the target frequency.
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

    /**
     * Simple label for debugging and future feedback display.
     */
    private String displayNameFor(int noteIndex, int divisions) {
        return divisions + "-TET scale degree -- " + noteIndex;
    }
}