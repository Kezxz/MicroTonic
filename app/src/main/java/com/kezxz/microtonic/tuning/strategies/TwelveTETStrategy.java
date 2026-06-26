package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.tuning.TuningStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * standard 12-tone equal temperament
 *
 * this is the familiar modern piano/guitar tuning where each octave is divided
 * into 12 equal steps
 */
public final class TwelveTETStrategy implements TuningStrategy {

    private static final String[] INTERVAL_QUALITIES = {
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
        return "12-tet";
    }

    @Override
    public String displayName() {
        return "12-TET";
    }

    @Override
    public TunedNote resolve(int noteIndex, TuningContext context) {
        double frequencyHz = context.tonicFrequencyHz() * Math.pow(2.0, noteIndex / 12.0);

        // find nearest MIDI note
        int nearestMidiNote = MusicMath.frequencyToNearestMidiNote(frequencyHz);

        // convert MIDI note to normal frequency to measure how far away the tuned frequency is
        double nearestMidiFrequency = MusicMath.midiNoteToFrequency(nearestMidiNote);

        // in 12-TET, should be 0
        double centsDeviation = MusicMath.centsBetween(frequencyHz, nearestMidiFrequency);

        int octaveOffset = Math.floorDiv(noteIndex, INTERVAL_QUALITIES.length);
        int scaleDegree = Math.floorMod(noteIndex, INTERVAL_QUALITIES.length);

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
            "12-Tet degree %d - %s - octave %d",
            displayDegree,
            INTERVAL_QUALITIES[scaleDegree],
            (octaveOffset + 1)
        );
    }
}