package com.kezxz.microtonic.sound.midi;

/**
 * Converts cents offsets into standard 14-bit MIDI pitch bend values.
 *
 * MIDI pitch bend values range from:
 * - 0     = maximum bend down
 * - 8192  = center / no bend
 * - 16383 = maximum bend up
 *
 * Important:
 * Pitch bend is channel-wide in normal MIDI. Later, this is why we will need
 * one MIDI channel per active note for microtonal chords.
 */
public final class PitchBendCalculator {

    public static final int MIN_PITCH_BEND = 0;
    public static final int CENTER_PITCH_BEND = 8192;
    public static final int MAX_PITCH_BEND = 16383;

    /**
     * Default General MIDI pitch bend range is often ±2 semitones.
     *
     * 2 semitones = 200 cents.
     *
     * Later, we may explicitly configure synth pitch bend sensitivity, but this
     * is the correct assumption for our first MIDI implementation.
     */
    public static final double DEFAULT_BEND_RANGE_CENTS = 200.0;

    private PitchBendCalculator() {
    }

    /**
     * Converts a cents offset into a MIDI pitch bend value using the default
     * ±200 cents range.
     *
     * Examples:
     * - 0 cents    -> 8192
     * - +100 cents -> halfway upward
     * - -100 cents -> halfway downward
     */
    public static int centsToPitchBend(double centsOffset) {
        return centsToPitchBend(centsOffset, DEFAULT_BEND_RANGE_CENTS);
    }

    /**
     * Converts a cents offset into a MIDI pitch bend value.
     *
     * bendRangeCents means the maximum pitch bend distance in one direction.
     *
     * Example:
     * If bendRangeCents is 200:
     * - -200 cents maps to 0
     * - 0 cents maps to 8192
     * - +200 cents maps close to 16383
     */
    public static int centsToPitchBend(double centsOffset, double bendRangeCents) {
        if (bendRangeCents <= 0.0) {
            throw new IllegalArgumentException("Bend range must be positive.");
        }

        double normalizedOffset = centsOffset / bendRangeCents;
        double rawValue = CENTER_PITCH_BEND + (normalizedOffset * CENTER_PITCH_BEND);

        return clampToPitchBendRange((int) Math.round(rawValue));
    }

    /**
     * Ensures the final value is legal for MIDI pitch bend.
     */
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