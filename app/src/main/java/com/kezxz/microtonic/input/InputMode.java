package com.kezxz.microtonic.input;

import java.util.Arrays;
import java.util.List;

// input sources available in the main input mode dropdown
public enum InputMode {
    MIDI("MIDI"),
    COMPUTER_KEYBOARD("Computer Keyboard");

    private final String displayName;

    InputMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(InputMode::displayName)
                .toList();
    }

    public static boolean isValidDisplayName(String displayName) {
        return displayNames().contains(displayName);
    }

    public static InputMode fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(inputMode -> inputMode.displayName.equals(displayName))
                .findFirst()
                .orElse(defaultMode());
    }

    public static InputMode defaultMode() {
        return COMPUTER_KEYBOARD;
    }
}