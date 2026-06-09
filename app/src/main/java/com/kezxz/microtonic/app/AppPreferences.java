package com.kezxz.microtonic.app;

import java.util.prefs.Preferences;

// saves and loads settings between app launches
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
        appState.setTuningSystem(preferences.get(KEY_TUNING_SYSTEM, appState.getTuningSystem()));
        appState.setTonic(preferences.get(KEY_TONIC, appState.getTonic()));
        appState.setNTetDivisions(preferences.getInt(KEY_N_TET_DIVISIONS, appState.getNTetDivisions()));
        appState.setInstrument(preferences.get(KEY_INSTRUMENT, appState.getInstrument()));
        appState.setInputMode(preferences.get(KEY_INPUT_MODE, appState.getInputMode()));
        appState.setWaveform(preferences.get(KEY_WAVEFORM, appState.getWaveform()));
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
}