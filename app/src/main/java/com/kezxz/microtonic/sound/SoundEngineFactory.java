package com.kezxz.microtonic.sound;

import com.kezxz.microtonic.sound.midi.MidiSoundEngine;

// creates current desktop sound engine
public final class SoundEngineFactory {
    
    private SoundEngineFactory() {
    }

    public static SoundEngine createDefault() {
        return new MidiSoundEngine();
    }
}