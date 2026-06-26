package com.kezxz.microtonic.sound.midi;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;

// allocates pitched MIDI channels for channel-per-note microtonal playback
public final class ChannelAllocator {

    public static final int MIDI_CHANNEL_COUNT = 16;

    // Java channel 9 is MIDI channel 10, commonly used for percussion
    public static final int PERCUSSION_CHANNEL = 9;

    private final Queue<Integer> availableChannels = new ArrayDeque<>();
    private final Set<Integer> allocatedChannels = new HashSet<>();

    public ChannelAllocator() {
        reset();
    }

    // allocates one available pitched MIDI channel
    public OptionalInt allocate() {
        Integer channel = availableChannels.poll();

        if (channel == null) {
            return OptionalInt.empty();
        }

        allocatedChannels.add(channel);
        return OptionalInt.of(channel);
    }

    // releases a previously allocated channel so it can be reused
    public void release(int channel) {
        if (!isUsablePitchedChannel(channel)) {
            return;
        }

        boolean wasAllocated = allocatedChannels.remove(channel);

        if (wasAllocated) {
            availableChannels.offer(channel);
        }
    }

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

    public static boolean isUsablePitchedChannel(int channel) {
        return channel >= 0
                && channel < MIDI_CHANNEL_COUNT
                && channel != PERCUSSION_CHANNEL;
    }
}