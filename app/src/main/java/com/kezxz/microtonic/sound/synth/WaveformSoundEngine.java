package com.kezxz.microtonic.sound.synth;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.sound.SoundEngine;
import com.kezxz.microtonic.sound.Waveform;
import com.kezxz.microtonic.tuning.TunedNote;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// SoundEngine implementation backed by Java's built-in MIDI synthesizer
// uses one lightweight playback thread per active synth voice
public final class WaveformSoundEngine implements SoundEngine {

    private static final float SAMPLE_RATE = 44_100.0f;
    private static final int BUFFER_FRAMES = 512;
    private static final double MAX_AMPLITUDE = 0.18;
    private static final double ATTACK_SECONDS = 0.005;
    private static final double RELEASE_SECONDS = 0.035;

    private final AppState appState;
    private final Map<Integer, ActiveSynthVoice> activeVoices = new ConcurrentHashMap<>();

    public WaveformSoundEngine(AppState appState) {
        this.appState = appState;
    }

    @Override
    public void playTestNote(int inputNoteId, int noteIndex, TunedNote tunedNote) {
        noteOn(inputNoteId, noteIndex, tunedNote, 100);

        Thread stopThread = new Thread(() -> {
            try {
                Thread.sleep(700);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }

            noteOff(inputNoteId);
        }, "waveform-test-note-stop");

        stopThread.setDaemon(true);
        stopThread.start();
    }

    @Override
    public void noteOn(int inputNoteId, int noteIndex, TunedNote tunedNote, int velocity) {
        noteOff(inputNoteId);

        Waveform waveform = Waveform.fromDisplayName(appState.getWaveform());
        double amplitude = velocityToAmplitude(velocity);

        ActiveSynthVoice voice = new ActiveSynthVoice(
                inputNoteId,
                tunedNote.frequencyHz(),
                waveform,
                amplitude
        );

        activeVoices.put(inputNoteId, voice);
        voice.start();
    }

    @Override
    public void noteOff(int inputNoteId) {
        ActiveSynthVoice voice = activeVoices.remove(inputNoteId);

        if (voice != null) {
            voice.stop();
        }
    }

    @Override
    public void allNotesOff() {
        for (ActiveSynthVoice voice : activeVoices.values()) {
            voice.stop();
        }

        activeVoices.clear();
    }

    @Override
    public void setInstrumentByName(String displayName) { // waveform synthesis does not use General MIDI instruments
    }

    @Override
    public void close() {
        allNotesOff();
    }

    private double velocityToAmplitude(int velocity) {
        int clampedVelocity = Math.max(0, Math.min(127, velocity));
        return MAX_AMPLITUDE * (clampedVelocity / 127.0);
    }

    private static final class ActiveSynthVoice {

        private final int inputNoteId;
        private final double frequencyHz;
        private final Waveform waveform;
        private final double amplitude;

        private volatile boolean releaseRequested;
        private volatile long releaseStartSample = -1;

        private Thread playbackThread;

        private ActiveSynthVoice(int inputNoteId, double frequencyHz, Waveform waveform, double amplitude) {
            this.inputNoteId = inputNoteId;
            this.frequencyHz = frequencyHz;
            this.waveform = waveform;
            this.amplitude = amplitude;
        }

        private void start() {
            releaseRequested = false;
            releaseStartSample = -1;

            playbackThread = new Thread(this::play, "waveform-voice-" + inputNoteId);
            playbackThread.setDaemon(true);
            playbackThread.start();
        }

        private void stop() {
            releaseRequested = true;
        }

        private void play() {
            AudioFormat format = new AudioFormat(
                    SAMPLE_RATE,
                    16,
                    1,
                    true,
                    false
            );

            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format, BUFFER_FRAMES * 2);
                line.start();

                byte[] buffer = new byte[BUFFER_FRAMES * 2];
                long sampleIndex = 0;

                while (!isReleaseComplete(sampleIndex)) {
                    fillBuffer(buffer, sampleIndex);
                    line.write(buffer, 0, buffer.length);
                    sampleIndex += BUFFER_FRAMES;
                }

                line.drain();
            } catch (LineUnavailableException exception) {
                throw new IllegalStateException("Unable to play waveform voice.", exception);
            }
        }

        private void fillBuffer(byte[] buffer, long startingSampleIndex) {
            for (int frame = 0; frame < BUFFER_FRAMES; frame++) {
                long absoluteSampleIndex = startingSampleIndex + frame;
                double timeSeconds = absoluteSampleIndex / SAMPLE_RATE;
                double envelope = envelopeAt(absoluteSampleIndex);
                double sample = sampleAt(timeSeconds) * amplitude * envelope;
                short pcmSample = (short) (sample * Short.MAX_VALUE);

                buffer[frame * 2] = (byte) (pcmSample & 0xff);
                buffer[frame * 2 + 1] = (byte) ((pcmSample >> 8) & 0xff);
            }
        }

        private double envelopeAt(long sampleIndex) {
            double attackSamples = ATTACK_SECONDS * SAMPLE_RATE;

            if (sampleIndex < attackSamples) {
                return sampleIndex / attackSamples;
            }

            if (!releaseRequested) {
                return 1.0;
            }

            if (releaseStartSample < 0) {
                releaseStartSample = sampleIndex;
            }

            double releaseSamples = RELEASE_SECONDS * SAMPLE_RATE;
            double releaseProgress = (sampleIndex - releaseStartSample) / releaseSamples;

            return Math.max(0.0, 1.0 - releaseProgress);
        }

        private boolean isReleaseComplete(long sampleIndex) {
            if (!releaseRequested || releaseStartSample < 0) {
                return false;
            }

            double releaseSamples = RELEASE_SECONDS * SAMPLE_RATE;
            return sampleIndex - releaseStartSample >= releaseSamples;
        }

        private double sampleAt(double timeSeconds) {
            double phase = (timeSeconds * frequencyHz) % 1.0;

            return switch (waveform) {
                case SINE -> Math.sin(2.0 * Math.PI * phase);
                case SQUARE -> phase < 0.5 ? 1.0 : -1.0;
                case SAW -> 2.0 * phase - 1.0;
                case TRIANGLE -> 1.0 - 4.0 * Math.abs(phase - 0.5);
            };
        }
    }
}