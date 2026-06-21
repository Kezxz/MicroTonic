package com.kezxz.microtonic.sound;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.sound.midi.MidiSoundEngine;
import com.kezxz.microtonic.sound.synth.WaveformSoundEngine;

// creates current desktop sound engine
public final class SoundEngineFactory {

    private SoundEngineFactory() {
    }

    public static SoundEngine createDefault() {
        return new MidiSoundEngine();
    }

    public static SoundEngine createWaveform(AppState appState) {
        return new WaveformSoundEngine(appState);
    }    
}