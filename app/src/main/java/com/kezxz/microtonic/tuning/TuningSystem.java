package com.kezxz.microtonic.tuning;

import java.util.Arrays;
import java.util.List;

public enum TuningSystem {
    TWELVE_TET("12-tet", "12-TET"),
    JUST_INTONATION_CHROMATIC("ji-12-chromatic", "Just Intonation - 12-tone chromatic"),
    PYTHAGOREAN("pythagorean", "Pythagorean"),
    MEANTONE("meantone", "Meantone"),
    N_TET("n-tet", "N-TET");

    private final String id;
    private final String displayName;

    TuningSystem(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * Later this can be used for presets, saved settings, or config files.
     */
    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(TuningSystem::displayName)
                .toList();
    }

    /**
     * Converts a UI display name into a TuningSystem.
     *
     * Unknown names safely fall back to 12-TET.
     */
    public static TuningSystem fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(system -> system.displayName.equals(displayName))
                .findFirst()
                .orElse(defaultSystem());
    }

    public static TuningSystem defaultSystem() {
        return TWELVE_TET;
    }
}