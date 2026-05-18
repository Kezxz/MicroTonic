package com.kezxz.microtonic.app;

import com.kezxz.microtonic.sound.GeneralMidiInstruments;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Holds the current user-selected application settings.
 *
 * JavaFX properties are used here so UI controls can bind directly to this state.
 * For example, when the user changes the tuning dropdown, the tuningSystem property updates automatically.
 *
 * This class does not perform tuning, MIDI, or sound logic. It only stores state.
 */
public final class AppState {

    private final StringProperty tuningSystem = new SimpleStringProperty("12-TET");                  // currently seleceted tuning system
    private final StringProperty tonic = new SimpleStringProperty("C");                             // currently selected tonic
    private final IntegerProperty nTetDivisions = new SimpleIntegerProperty(12);                   // number of divisions for N-TET tuning
    private final StringProperty instrument = new SimpleStringProperty(
            GeneralMidiInstruments.defaultInstrument().displayName()                                          // currently selected instrument
    );
    private final StringProperty inputMode = new SimpleStringProperty("Computer Keyboard");    // currently selected input mode
    private final StringProperty waveform = new SimpleStringProperty("Sine");                 // currently selected waveform

    /**
     * Returns the JavaFX property itself so UI controls can bind to it.
     */
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