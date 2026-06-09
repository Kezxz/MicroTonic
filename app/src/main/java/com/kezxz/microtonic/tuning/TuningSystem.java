package com.kezxz.microtonic.tuning;

import java.util.Arrays;
import java.util.List;

// supported tuning systems shown in the main tuning dropdown
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

    // converts a UI display name into a TuningSystem, falls back to 12-TET
    public static TuningSystem fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(system -> system.displayName.equals(displayName))
                .findFirst()
                .orElse(defaultSystem());
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayNames().contains(displayName);
    }

    public static TuningSystem defaultSystem() {
        return TWELVE_TET;
    }
}