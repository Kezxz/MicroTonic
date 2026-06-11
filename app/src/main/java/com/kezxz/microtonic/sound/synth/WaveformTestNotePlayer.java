package com.kezxz.microtonic.sound.synth;

import com.kezxz.microtonic.sound.Waveform;
import com.kezxz.microtonic.tuning.TunedNote;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

// short waveform preview used by the test-note button
public final class WaveformTestNotePlayer {

    private static final float SAMPLE_RATE = 44_100.0f;
    private static final int DURATION_MS = 700;
    private static final double AMPLITUDE = 0.25;

    public void play(TunedNote tunedNote, String waveformDisplayName) {
        Waveform waveform = Waveform.fromDisplayName(waveformDisplayName);

        Thread playbackThread = new Thread(
                () -> playOnCurrentThread(tunedNote.frequencyHz(), waveform),
                "waveform-test-note"
        );
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    private void playOnCurrentThread(double frequencyHz, Waveform waveform) {
        AudioFormat format = new AudioFormat(
            SAMPLE_RATE,
            16,
            1,
            true,
            false
        );

        byte[] audioBuffer = createAudioBuffer(frequencyHz, waveform);

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(audioBuffer, 0, audioBuffer.length);
            line.drain();
        } catch (LineUnavailableException e) {
            throw new IllegalStateException("Unable to play waveform test note.", e);
        }
    }

    private byte[] createAudioBuffer(double frequencyHz, Waveform waveform) {
        int sampleCount = Math.round(SAMPLE_RATE * DURATION_MS / 1000.0f);
        byte[] buffer = new byte[sampleCount * 2];

        for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
            double timeSeconds = sampleIndex / SAMPLE_RATE;
            double sample = sampleAt(timeSeconds, frequencyHz, waveform);
            short pcmSample = (short) (sample * Short.MAX_VALUE * AMPLITUDE);

            buffer[sampleIndex * 2] = (byte) (pcmSample & 0xff);
            buffer[sampleIndex * 2 + 1] = (byte) ((pcmSample >> 8) & 0xff);
        }

        return buffer;
    }

    private double sampleAt(double timeSeconds, double frequencyHz, Waveform waveform) {
        double phase = (timeSeconds * frequencyHz) % 1.0;

        return switch (waveform) {
            case SINE -> Math.sin(2.0 * Math.PI * phase);
            case SQUARE -> phase < 0.5 ? 1.0 : -1.0;
            case SAW -> 2.0 * phase - 1.0;
            case TRIANGLE -> 1.0 - 4.0 * Math.abs(phase - 0.5);
        };
    }
}