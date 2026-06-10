package com.kezxz.microtonic.app;

import com.kezxz.microtonic.sound.GeneralMidiInstruments;
import com.kezxz.microtonic.sound.Waveform;
import com.kezxz.microtonic.tuning.TuningSystem;
import com.kezxz.microtonic.input.InputMode;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * in-memory model for user-seleced app settings
 */
public final class AppState {

    private final StringProperty tuningSystem = new SimpleStringProperty(
            TuningSystem.defaultSystem().displayName()
    );
    private final StringProperty tonic = new SimpleStringProperty("C");
    private final IntegerProperty nTetDivisions = new SimpleIntegerProperty(12);
    private final StringProperty instrument = new SimpleStringProperty(
            GeneralMidiInstruments.defaultInstrument().displayName()
    );
    private final StringProperty inputMode = new SimpleStringProperty(
            InputMode.defaultMode().displayName()
    );
    private final StringProperty waveform = new SimpleStringProperty(
            Waveform.defaultWaveform().displayName()
    );

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

    // clamp saved or user-entered values to the supported UI range
    public void setNTetDivisions(int nTetDivisions) {
        if (nTetDivisions < 2) {
            this.nTetDivisions.set(2);
            return;
        }

        if (nTetDivisions > 72) {
            this.nTetDivisions.set(72);
            return;
        }

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