package com.kezxz.microtonic.tuning;

/**
 * Represents the 12 pitch classes used by the app's tonic selector.
 *
 * Enharmonic spellings are grouped together:
 * - C#/Db is one pitch class
 * - D#/Eb is one pitch class
 * - etc.
 */
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

    private final String displayName; // text shown in the UI

    // Distance from C in normal chromatic semitones.
    // This will help convert tonic choices to reference frequencies later.
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

    /**
     * Converts a UI display name into a PitchClass.
     *
     * Example:
     * - "C" becomes PitchClass.C
     * - "F#/Gb" becomes PitchClass.F_SHARP_G_FLAT
     */
    public static PitchClass fromDisplayName(String displayName) {
        for (PitchClass pitchClass : values()) {
            if (pitchClass.displayName.equals(displayName)) {
                return pitchClass;
            }
        }

        throw new IllegalArgumentException("Unknown pitch class: " + displayName);
    }
}