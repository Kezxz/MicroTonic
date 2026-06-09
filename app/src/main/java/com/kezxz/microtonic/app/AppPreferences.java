package com.kezxz.microtonic.app;

import com.kezxz.microtonic.sound.GeneralMidiInstruments;
import com.kezxz.microtonic.tuning.PitchClass;
import com.kezxz.microtonic.tuning.TuningSystem;

import java.util.prefs.Preferences;

/**
 * saves and loads settings between app launches
 */
public final class AppPreferences {

    private static final String KEY_TUNING_SYSTEM = "tuningSystem";
    private static final String KEY_TONIC = "tonic";
    private static final String KEY_N_TET_DIVISIONS = "nTetDivisions";
    private static final String KEY_INSTRUMENT = "instrument";
    private static final String KEY_INPUT_MODE = "inputMode";
    private static final String KEY_WAVEFORM = "waveform";

    private final Preferences preferences = Preferences.userNodeForPackage(AppPreferences.class);

    // missing saved values fall back to current AppState defaults
    public void loadInto(AppState appState) {
        appState.setTuningSystem(validTuningSystem(
                preferences.get(KEY_TUNING_SYSTEM, appState.getTuningSystem())
        ));
        appState.setTonic(validTonic(
                preferences.get(KEY_TONIC, appState.getTonic())
        ));
        appState.setNTetDivisions(
                preferences.getInt(KEY_N_TET_DIVISIONS, appState.getNTetDivisions())
        );
        appState.setInstrument(validInstrument(
                preferences.get(KEY_INSTRUMENT, appState.getInstrument())
        ));
        appState.setInputMode(validInputMode(
                preferences.get(KEY_INPUT_MODE, appState.getInputMode())
        ));
        appState.setWaveform(validWaveform(
                preferences.get(KEY_WAVEFORM, appState.getWaveform())
        ));
    }

    // saves current app state
    public void save(AppState appState) {
        preferences.put(KEY_TUNING_SYSTEM, appState.getTuningSystem());
        preferences.put(KEY_TONIC, appState.getTonic());
        preferences.putInt(KEY_N_TET_DIVISIONS, appState.getNTetDivisions());
        preferences.put(KEY_INSTRUMENT, appState.getInstrument());
        preferences.put(KEY_INPUT_MODE, appState.getInputMode());
        preferences.put(KEY_WAVEFORM, appState.getWaveform());
    }

// ----------- PREFERENCE VALIDATION HELPERS ----------- //

    private String validTuningSystem(String value) {
        if (TuningSystem.isValidDisplayName(value)) {
            return value;
        }

        return TuningSystem.defaultSystem().displayName();
    }

    private String validTonic(String value) {
        if (PitchClass.isValidDisplayName(value)) {
            return value;
        }

        return PitchClass.defaultPitchClass().displayName();
    }

    private String validInstrument(String value) {
        if (GeneralMidiInstruments.isValidDisplayName(value)) {
            return value;
        }

        return GeneralMidiInstruments.defaultInstrument().displayName();
    }

    private String validInputMode(String value) {
        if ("MIDI".equals(value) || "Computer Keyboard".equals(value)) {
            return value;
        }

        return "Computer Keyboard";
    }

    private String validWaveform(String value) {
        if ("Sine".equals(value)
                || "Square".equals(value)
                || "Saw".equals(value)
                || "Triangle".equals(value)) {
            return value;
        }

        return "Sine";
    }
}