package com.kezxz.microtonic.util;

/**
 * Shared music/math helpers used by tuning and sound code.
 *
 * This class is stateless, so every method is static.
 */
public final class MusicMath {

    public static final double A4_FREQUENCY_HZ = 440.0; // standard tuning reference for A4

    public static final int A4_MIDI_NOTE = 69; // in MIDI, A4 is note number 69

    // Middle C frequency in 12-TET when A4 = 440 Hz.
    public static final double C4_FREQUENCY_HZ = 261.6255653005986; // middle C in 12-TET when A4 = 440 Hz

    /**
     * Private constructor prevents creating instances of this utility class.
     */
    private MusicMath() {
    }

    /**
     * Converts a MIDI note number to its standard 12-TET frequency.
     *
     * Example:
     * - MIDI 69 = A4 = 440 Hz
     * - MIDI 60 = C4 ≈ 261.63 Hz
     */
    public static double midiNoteToFrequency(int midiNote) {
        return A4_FREQUENCY_HZ * Math.pow(2.0, (midiNote - A4_MIDI_NOTE) / 12.0);
    }

    /**
     * Finds the nearest standard MIDI note for a given frequency.
     *
     * This will be important later for pitch bend:
     * - choose the nearest normal MIDI note
     * - bend it up/down to the microtonal target frequency
     */
    public static int frequencyToNearestMidiNote(double frequencyHz) {
        validatePositiveFrequency(frequencyHz);

        return (int) Math.round(A4_MIDI_NOTE + 12.0 * log2(frequencyHz / A4_FREQUENCY_HZ));
    }

    /**
     * Calculates the distance between two frequencies in cents.
     *
     * 1200 cents = one octave.
     * 100 cents = one 12-TET semitone.
     *
     * Positive result means frequencyAHz is higher than frequencyBHz.
     * Negative result means frequencyAHz is lower than frequencyBHz.
     */
    public static double centsBetween(double frequencyAHz, double frequencyBHz) {
        validatePositiveFrequency(frequencyAHz);
        validatePositiveFrequency(frequencyBHz);

        return 1200.0 * log2(frequencyAHz / frequencyBHz);
    }

    /**
     * Converts a cents value to a frequency ratio.
     *
     * Example:
     * - 1200 cents = ratio 2.0
     * - 700 cents ≈ ratio of a perfect fifth in 12-TET
     */
    public static double centsToRatio(double cents) {
        return Math.pow(2.0, cents / 1200.0);
    }

    /**
     * Converts a frequency ratio to cents.
     *
     * Example:
     * - ratio 2.0 = 1200 cents
     */
    public static double ratioToCents(double ratio) {
        if (ratio <= 0.0) {
            throw new IllegalArgumentException("Ratio must be positive.");
        }

        return 1200.0 * log2(ratio);
    }

    /**
     * Java has natural log built in, but not log base 2 directly.
     */
    public static double log2(double value) {
        if (value <= 0.0) {
            throw new IllegalArgumentException("Value must be positive.");
        }

        return Math.log(value) / Math.log(2.0);
    }

    /**
     * Shared validation helper for frequency inputs.
     */
    private static void validatePositiveFrequency(double frequencyHz) {
        if (frequencyHz <= 0.0) {
            throw new IllegalArgumentException("Frequency must be positive.");
        }
    }
}