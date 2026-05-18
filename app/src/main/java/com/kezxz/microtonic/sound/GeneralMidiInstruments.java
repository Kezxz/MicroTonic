package com.kezxz.microtonic.sound;

import java.util.Arrays;
import java.util.List;

/**
 * General MIDI instrument definitions used by MicroTonic.
 *
 * Java's MidiChannel.programChange(int) uses zero-based program numbers:
 * - 0  = Acoustic Grand Piano
 * - 11 = Vibraphone
 * - 40 = Violin
 * - etc.
 *
 * Each instrument is declared once with:
 * - a display name for the UI
 * - a General MIDI program number for playback
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

    /**
     * Returns all display names for use in JavaFX dropdowns.
     */
    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(GeneralMidiInstruments::displayName)
                .toList();
    }

    /**
     * Converts a UI display name into a General MIDI program number.
     *
     * Unknown names safely fall back to Acoustic Grand Piano.
     */
    public static int programForDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(instrument -> instrument.displayName.equals(displayName))
                .findFirst()
                .map(GeneralMidiInstruments::program)
                .orElse(ACOUSTIC_GRAND_PIANO.program);
    }

    /**
     * Default instrument used when the app starts.
     */
    public static GeneralMidiInstruments defaultInstrument() {
        return ACOUSTIC_GRAND_PIANO;
    }
}