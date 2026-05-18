package com.kezxz.microtonic.sound.midi;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.sound.GeneralMidiInstruments;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sound engine backed by Java's built-in MIDI synthesizer.
 *
 * This is our first real sound output path.
 *
 * Important MVP behavior:
 * - uses General MIDI instruments
 * - starts with Acoustic Grand Piano
 * - applies pitch bend before note-on
 * - uses VoiceManager so each active note can get its own MIDI channel
 *
 * This class is intentionally small right now. We will improve it as we add
 * keyboard input, MIDI input, instrument selection, and panic/all-notes-off.
 */
public final class MidiSoundEngine implements AutoCloseable {

    private static final int DEFAULT_INSTRUMENT_PROGRAM = 0; // Acoustic Grand Piano
    private static final int DEFAULT_VELOCITY = 100;
    private static final int TEST_NOTE_DURATION_MS = 900;

    private final ChannelAllocator channelAllocator = new ChannelAllocator();
    private final VoiceManager voiceManager = new VoiceManager(channelAllocator);
    private final ScheduledExecutorService noteOffScheduler = Executors.newSingleThreadScheduledExecutor();

    private Synthesizer synthesizer;
    private MidiChannel[] channels;
    private int currentInstrumentProgram = DEFAULT_INSTRUMENT_PROGRAM;

    private boolean started;

    /**
     * Opens the Java MIDI synthesizer if it is not already open.
     */
    public void start() {
        if (started) {
            return;
        }

        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channels = synthesizer.getChannels();

            if (channels == null || channels.length == 0) {
                throw new IllegalStateException("No MIDI channels available from synthesizer.");
            }

            setInstrumentProgram(currentInstrumentProgram);
            started = true;
        } catch (MidiUnavailableException exception) {
            throw new IllegalStateException("Unable to open Java MIDI synthesizer.", exception);
        }
    }

    /**
     * Plays one short note for manual testing from the debug UI.
     *
     * Later, real keyboard/MIDI input will call noteOn and noteOff directly.
     */
    public void playTestNote(int inputNoteId, int noteIndex, TunedNote tunedNote) {
        noteOn(inputNoteId, noteIndex, tunedNote, DEFAULT_VELOCITY);

        noteOffScheduler.schedule(
                () -> noteOff(inputNoteId),
                TEST_NOTE_DURATION_MS,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Starts a note using the tuned note information.
     *
     * The tuned note already contains:
     * - nearest MIDI note
     * - cents deviation from that MIDI note
     * - frequency metadata
     */
    public void noteOn(int inputNoteId, int noteIndex, TunedNote tunedNote, int velocity) {
        ensureStarted();

        VoiceManager.VoiceAllocation allocation = voiceManager.startVoice(
                inputNoteId,
                noteIndex,
                tunedNote,
                velocity
        );

        stopVoiceIfPresent(Optional.ofNullable(allocation.replacedVoice()));
        stopVoiceIfPresent(Optional.ofNullable(allocation.stolenVoice()));

        Voice voice = allocation.newVoice();
        MidiChannel channel = channels[voice.channel()];

        int pitchBend = PitchBendCalculator.centsToPitchBend(
                tunedNote.centsDeviationFromNearest12Tet()
        );

        channel.programChange(currentInstrumentProgram);
        channel.setPitchBend(pitchBend);
        channel.noteOn(voice.midiNote(), voice.velocity());
    }

    /**
     * Stops a note by its logical input note ID.
     */
    public void noteOff(int inputNoteId) {
        ensureStarted();

        Optional<Voice> releasedVoice = voiceManager.releaseVoice(inputNoteId);
        stopVoiceIfPresent(releasedVoice);
    }

    /**
     * Stops all currently active voices.
     */
    public void allNotesOff() {
        if (!started || channels == null) {
            return;
        }

        for (MidiChannel channel : channels) {
            if (channel != null) {
                channel.allNotesOff();
                channel.setPitchBend(PitchBendCalculator.CENTER_PITCH_BEND);
            }
        }

        voiceManager.releaseAll();
    }

    /**
     * Sets the current instrument by UI display name.
     */
    public void setInstrumentByName(String displayName) {
        setInstrumentProgram(GeneralMidiInstruments.programForDisplayName(displayName));
}

    /**
     * Sets the same General MIDI program on all pitched channels. 0 = Acoustic Piano
     */
    public void setInstrumentProgram(int program) {
        if (program < 0 || program > 127) {
            throw new IllegalArgumentException("MIDI program must be between 0 and 127.");
        }

        currentInstrumentProgram = program;

        if (channels == null) {
            return;
        }

        for (int channelIndex = 0; channelIndex < channels.length; channelIndex++) {
            if (ChannelAllocator.isUsablePitchedChannel(channelIndex) && channels[channelIndex] != null) {
                channels[channelIndex].programChange(program);
            }
        }
    }

    private void stopVoiceIfPresent(Optional<Voice> voice) {
        voice.ifPresent(this::stopVoice);
    }

    private void stopVoice(Voice voice) {
        if (channels == null || voice.channel() >= channels.length) {
            return;
        }

        MidiChannel channel = channels[voice.channel()];

        if (channel == null) {
            return;
        }

        channel.noteOff(voice.midiNote());
        channel.setPitchBend(PitchBendCalculator.CENTER_PITCH_BEND);
    }

    private void ensureStarted() {
        if (!started) {
            start();
        }
    }

    @Override
    public void close() {
        allNotesOff();
        noteOffScheduler.shutdownNow();

        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }

        started = false;
    }
}