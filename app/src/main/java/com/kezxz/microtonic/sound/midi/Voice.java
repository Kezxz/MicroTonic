package com.kezxz.microtonic.sound.midi;

import java.time.Instant;

/**
 * Represents one currently sounding note.
 *
 * A Voice connects:
 * - the logical input note, such as keyboard key or MIDI note
 * - the tuned note's MIDI note number
 * - the allocated MIDI channel
 * - timing information used for simple voice stealing
 */
public record Voice(
        long voiceId,
        int inputNoteId,
        int noteIndex,
        int midiNote,
        int channel,
        double frequencyHz,
        int velocity,
        Instant startedAt
) {
    public Voice {
        if (voiceId < 0) {
            throw new IllegalArgumentException("Voice ID cannot be negative.");
        }

        if (!ChannelAllocator.isUsablePitchedChannel(channel)) {
            throw new IllegalArgumentException("Invalid pitched MIDI channel: " + channel);
        }

        if (frequencyHz <= 0.0) {
            throw new IllegalArgumentException("Frequency must be positive.");
        }

        if (velocity < 0 || velocity > 127) {
            throw new IllegalArgumentException("Velocity must be between 0 and 127.");
        }

        if (startedAt == null) {
            throw new IllegalArgumentException("Started time cannot be null.");
        }
    }
}