package com.kezxz.microtonic.sound;

import com.kezxz.microtonic.tuning.TunedNote;

// playback engine, each platform provides its own implementation
public interface SoundEngine extends AutoCloseable {

    void playTestNote(int inputNoteId, int noteIndex, TunedNote tunedNote);

    void noteOn(int inputNoteId, int noteIndex, TunedNote tunedNote, int velocity);

    void noteOff(int inputNoteId);

    void allNotesOff();

    void setInstrumentByName(String displayName);

    @Override
    void close();
}