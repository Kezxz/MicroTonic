package com.kezxz.microtonic.util;

// shared music/math helpers
public final class MusicMath {

    public static final double A4_FREQUENCY_HZ = 440.0; // standard tuning reference for A4
    public static final int A4_MIDI_NOTE = 69; // in MIDI, A4 is note number 69
    public static final double C4_FREQUENCY_HZ = 261.6255653005986; // middle C in 12-TET when A4 = 440 Hz

    private MusicMath() {
    }

     // converts MIDI note number to its standard 12-TET frequency
    public static double midiNoteToFrequency(int midiNote) {
        return A4_FREQUENCY_HZ * Math.pow(2.0, (midiNote - A4_MIDI_NOTE) / 12.0);
    }

    // finds the nearest MIDI note for a given frequency
    public static int frequencyToNearestMidiNote(double frequencyHz) {
        validatePositiveFrequency(frequencyHz);

        return (int) Math.round(A4_MIDI_NOTE + 12.0 * log2(frequencyHz / A4_FREQUENCY_HZ));
    }

    // calculates the distance between two frequencies in cents
    public static double centsBetween(double frequencyAHz, double frequencyBHz) {
        validatePositiveFrequency(frequencyAHz);
        validatePositiveFrequency(frequencyBHz);

        return 1200.0 * log2(frequencyAHz / frequencyBHz);
    }

    // converts a cents value to a frequency ratio
    public static double centsToRatio(double cents) {
        return Math.pow(2.0, cents / 1200.0);
    }

    // converts a frequency ratio to cents
    public static double ratioToCents(double ratio) {
        if (ratio <= 0.0) {
            throw new IllegalArgumentException("Ratio must be positive.");
        }

        return 1200.0 * log2(ratio);
    }

    public static double log2(double value) {
        if (value <= 0.0) {
            throw new IllegalArgumentException("Value must be positive.");
        }

        return Math.log(value) / Math.log(2.0);
    }

    private static void validatePositiveFrequency(double frequencyHz) {
        if (frequencyHz <= 0.0) {
            throw new IllegalArgumentException("Frequency must be positive.");
        }
    }
}