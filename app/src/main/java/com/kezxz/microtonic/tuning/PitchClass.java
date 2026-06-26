package com.kezxz.microtonic.tuning;

import java.util.Arrays;
import java.util.List;

// represents the 12 pitch classes used by the tonic selector
// enharmonic spellings are grouped together
public enum PitchClass {
    C("C", 0),
    C_SHARP_D_FLAT("C#/Db", 1),
    D("D", 2),
    D_SHARP_E_FLAT("D#/Eb", 3),
    E("E", 4),
    F("F", 5),
    F_SHARP_G_FLAT("F#/Gb", 6),
    G("G", 7),
    G_SHARP_A_FLAT("G#/Ab", 8),
    A("A", 9),
    A_SHARP_B_FLAT("A#/Bb", 10),
    B("B", 11);

    private final String displayName;

    // distance from C in 12-TET semitones, used for tonic frequency lookup
    private final int semitoneOffsetFromC;

    PitchClass(String displayName, int semitoneOffsetFromC) {
        this.displayName = displayName;
        this.semitoneOffsetFromC = semitoneOffsetFromC;
    }

    public String displayName() {
        return displayName;
    }

    public int semitoneOffsetFromC() {
        return semitoneOffsetFromC;
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(PitchClass::displayName)
                .toList();
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayNames().contains(displayName);
    }

    public static PitchClass defaultPitchClass() {
        return C;
    }

    // converts a UI display name into a PitchClass -- "F#/Gb" becomes PitchClass.F_SHARP_G_FLAT
    public static PitchClass fromDisplayName(String displayName) {
        for (PitchClass pitchClass : values()) {
            if (pitchClass.displayName.equals(displayName)) {
                return pitchClass;
            }
        }

        throw new IllegalArgumentException("Unknown pitch class: " + displayName);
    }
}