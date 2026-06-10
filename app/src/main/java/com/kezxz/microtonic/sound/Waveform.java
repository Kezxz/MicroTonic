package com.kezxz.microtonic.sound;

import java.util.Arrays;
import java.util.List;

/**
 * oscillator waveforms available in the waveform dropdown
 */
public enum Waveform {
    SINE("Sine"),
    SQUARE("Square"),
    SAW("Saw"),
    TRIANGLE("Triangle");

    private final String displayName;

    Waveform(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(Waveform::displayName)
                .toList();
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayNames().contains(displayName);
    }

    public static Waveform fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(waveform -> waveform.displayName.equals(displayName))
                .findFirst()
                .orElse(defaultWaveform());
    }

    public static Waveform defaultWaveform() {
        return SINE;
    }
}