package com.kezxz.microtonic.app;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Holds the current user-selected application settings.
 *
 * This is intentionally simple for now. Later, other parts of the app will read
 * from this state when deciding how to tune notes and produce sound.
 */
public final class AppState {

    private final StringProperty tuningSystem = new SimpleStringProperty("12-TET");
    private final StringProperty tonic = new SimpleStringProperty("C");
    private final IntegerProperty nTetDivisions = new SimpleIntegerProperty(12);
    private final StringProperty instrument = new SimpleStringProperty("Acoustic Grand Piano");
    private final StringProperty inputMode = new SimpleStringProperty("Computer Keyboard");
    private final StringProperty waveform = new SimpleStringProperty("Sine");

    public StringProperty tuningSystemProperty() {
        return tuningSystem;
    }

    public String getTuningSystem() {
        return tuningSystem.get();
    }

    public void setTuningSystem(String tuningSystem) {
        this.tuningSystem.set(tuningSystem);
    }

    public StringProperty tonicProperty() {
        return tonic;
    }

    public String getTonic() {
        return tonic.get();
    }

    public void setTonic(String tonic) {
        this.tonic.set(tonic);
    }

    public IntegerProperty nTetDivisionsProperty() {
        return nTetDivisions;
    }

    public int getNTetDivisions() {
        return nTetDivisions.get();
    }

    public void setNTetDivisions(int nTetDivisions) {
        this.nTetDivisions.set(nTetDivisions);
    }

    public StringProperty instrumentProperty() {
        return instrument;
    }

    public String getInstrument() {
        return instrument.get();
    }

    public void setInstrument(String instrument) {
        this.instrument.set(instrument);
    }

    public StringProperty inputModeProperty() {
        return inputMode;
    }

    public String getInputMode() {
        return inputMode.get();
    }

    public void setInputMode(String inputMode) {
        this.inputMode.set(inputMode);
    }

    public StringProperty waveformProperty() {
        return waveform;
    }

    public String getWaveform() {
        return waveform.get();
    }

    public void setWaveform(String waveform) {
        this.waveform.set(waveform);
    }
}