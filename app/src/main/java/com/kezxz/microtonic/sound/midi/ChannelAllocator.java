package com.kezxz.microtonic.sound.midi;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;

/**
 * Allocates MIDI channels for active notes.
 *
 * Why this exists:
 * MIDI pitch bend affects an entire channel, not just one note.
 * For microtonal polyphony, each active note should get its own MIDI channel
 * so each note can have an independent pitch bend value.
 *
 * Standard MIDI has 16 channels:
 * - channel 0 through 15 in Java's zero-based indexing
 * - channel 9 is commonly the percussion/drum channel, so we skip it
 */
public final class ChannelAllocator {

    public static final int MIDI_CHANNEL_COUNT = 16;

    /**
     * Java MIDI channels are zero-based.
     *
     * Channel 9 corresponds to MIDI channel 10, which is commonly percussion.
     */
    public static final int PERCUSSION_CHANNEL = 9;

    private final Queue<Integer> availableChannels = new ArrayDeque<>();
    private final Set<Integer> allocatedChannels = new HashSet<>();

    public ChannelAllocator() {
        reset();
    }

    /**
     * Allocates one available pitched MIDI channel.
     *
     * Returns OptionalInt.empty() when no channel is available.
     *
     * Later, VoiceManager will decide whether to steal an older voice if this
     * returns empty.
     */
    public OptionalInt allocate() {
        Integer channel = availableChannels.poll();

        if (channel == null) {
            return OptionalInt.empty();
        }

        allocatedChannels.add(channel);
        return OptionalInt.of(channel);
    }

    /**
     * Releases a previously allocated channel so it can be reused.
     *
     * If the channel was not allocated, this method does nothing.
     */
    public void release(int channel) {
        if (!isUsablePitchedChannel(channel)) {
            return;
        }

        boolean wasAllocated = allocatedChannels.remove(channel);

        if (wasAllocated) {
            availableChannels.offer(channel);
        }
    }

    /**
     * Resets the allocator back to its initial state.
     */
    public void reset() {
        availableChannels.clear();
        allocatedChannels.clear();

        for (int channel = 0; channel < MIDI_CHANNEL_COUNT; channel++) {
            if (isUsablePitchedChannel(channel)) {
                availableChannels.offer(channel);
            }
        }
    }

    public int availableCount() {
        return availableChannels.size();
    }

    public int allocatedCount() {
        return allocatedChannels.size();
    }

    public boolean isAllocated(int channel) {
        return allocatedChannels.contains(channel);
    }

    /**
     * Returns true only for valid pitched MIDI channels.
     */
    public static boolean isUsablePitchedChannel(int channel) {
        return channel >= 0
                && channel < MIDI_CHANNEL_COUNT
                && channel != PERCUSSION_CHANNEL;
    }
}