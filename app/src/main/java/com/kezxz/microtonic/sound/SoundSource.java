package com.kezxz.microtonic.sound;

import java.util.Arrays;
import java.util.List;

// playback sources available in the main sound source dropdown
public enum SoundSource {
    GENERAL_MIDI("General Midi"),
    SYNTH_WAVEFORM("Synth Waveform");

    private final String displayName;

    SoundSource(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(SoundSource::displayName)
                .toList();
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayNames().contains(displayName);
    }

    public static SoundSource fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(soundSource -> soundSource.displayName.equals(displayName))
                .findFirst()
                .orElse(defaultSource());
    }

    public static SoundSource defaultSource() {
        return GENERAL_MIDI;
    }
}