package com.kezxz.microtonic.sound;

import java.util.Arrays;
import java.util.List;

/**
 * general MIDI instruments available in the instrument dropdown
 *
 * program numbers are zero-based for Java's MidiChannel API
 */
public enum GeneralMidiInstruments {
    ACOUSTIC_GRAND_PIANO("Acoustic Grand Piano", 0),
    ELECTRIC_PIANO_1("Electric Piano 1", 4),
    VIBRAPHONE("Vibraphone", 11),
    ELECTRIC_BASS_FINGER("Electric Bass", 33),
    VIOLIN("Violin", 40),
    ALTO_SAX("Alto Sax", 65),
    FLUTE("Flute", 73),
    SITAR("Sitar", 104);

    private final String displayName;
    private final int program;

    GeneralMidiInstruments(String displayName, int program) {
        this.displayName = displayName;
        this.program = program;
    }

    public String displayName() {
        return displayName;
    }

    public int program() {
        return program;
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(GeneralMidiInstruments::displayName)
                .toList();
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayNames().contains(displayName);
    }

    // converts display name into a general MIDI program number -- unknown name falls back to Acoustic Grand Piano
    public static int programForDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(instrument -> instrument.displayName.equals(displayName))
                .findFirst()
                .map(GeneralMidiInstruments::program)
                .orElse(ACOUSTIC_GRAND_PIANO.program);
    }

    public static GeneralMidiInstruments defaultInstrument() {
        return ACOUSTIC_GRAND_PIANO;
    }
}