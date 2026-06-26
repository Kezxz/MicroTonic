package com.kezxz.microtonic.sound.midi;

/**
 * converts cents offsets into standard 14-bit MIDI pitch bend values
 * 
 * pitch bend is channel-wide in MIDI
 * we need one channel per note for microtonal chords
 */
public final class PitchBendCalculator {

    // normal MIDI pitch bend range
    public static final int MIN_PITCH_BEND = 0;
    public static final int CENTER_PITCH_BEND = 8192;
    public static final int MAX_PITCH_BEND = 16383;

    // default MIDI pitch bend range: ±2 semitones (200 cents)
    public static final double DEFAULT_BEND_RANGE_CENTS = 200.0;

    private PitchBendCalculator() {
    }

    /**
     * converts a cents offset into a MIDI pitch bend value using the default
     * ±200 cents range
     *
     * delegates to the ranged version so pitch bend scaling stays consistent
     */
    public static int centsToPitchBend(double centsOffset) {
        return centsToPitchBend(centsOffset, DEFAULT_BEND_RANGE_CENTS);
    }

    /**
     * converts a cents offset into a MIDI pitch bend value
     *
     * MIDI pitch bend is centered at 8192
     * cents offset is scaled around that midpoint using bendRangeCents
     */
    public static int centsToPitchBend(double centsOffset, double bendRangeCents) {
        if (bendRangeCents <= 0.0) {
            throw new IllegalArgumentException("Bend range must be positive.");
        }

        double normalizedOffset = centsOffset / bendRangeCents;
        double rawValue = CENTER_PITCH_BEND + (normalizedOffset * CENTER_PITCH_BEND);

        return clampToPitchBendRange((int) Math.round(rawValue));
    }

    private static int clampToPitchBendRange(int value) {
        if (value < MIN_PITCH_BEND) {
            return MIN_PITCH_BEND;
        }

        if (value > MAX_PITCH_BEND) {
            return MAX_PITCH_BEND;
        }

        return value;
    }
}