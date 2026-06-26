package com.kezxz.microtonic.sound.midi;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelAllocatorTest {

    @Test
    void startsWithFifteenAvailablePitchedChannels() {
        ChannelAllocator allocator = new ChannelAllocator();

        assertEquals(15, allocator.availableCount());
        assertEquals(0, allocator.allocatedCount());
    }

    @Test
    void doesNotAllocatePercussionChannel() {
        ChannelAllocator allocator = new ChannelAllocator();
        Set<Integer> allocatedChannels = new HashSet<>();

        for (int i = 0; i < 15; i++) {
            OptionalInt channel = allocator.allocate();

            assertTrue(channel.isPresent());
            allocatedChannels.add(channel.getAsInt());
        }

        assertFalse(allocatedChannels.contains(ChannelAllocator.PERCUSSION_CHANNEL));
    }

    @Test
    void allocatesFifteenUniqueChannels() {
        ChannelAllocator allocator = new ChannelAllocator();
        Set<Integer> allocatedChannels = new HashSet<>();

        for (int i = 0; i < 15; i++) {
            OptionalInt channel = allocator.allocate();

            assertTrue(channel.isPresent());
            allocatedChannels.add(channel.getAsInt());
        }

        assertEquals(15, allocatedChannels.size());
        assertEquals(0, allocator.availableCount());
        assertEquals(15, allocator.allocatedCount());
    }

    @Test
    void returnsEmptyWhenNoChannelsRemain() {
        ChannelAllocator allocator = new ChannelAllocator();

        for (int i = 0; i < 15; i++) {
            allocator.allocate();
        }

        OptionalInt extraChannel = allocator.allocate();

        assertTrue(extraChannel.isEmpty());
    }

    @Test
    void releasedChannelCanBeAllocatedAgain() {
        ChannelAllocator allocator = new ChannelAllocator();

        OptionalInt firstChannel = allocator.allocate();
        assertTrue(firstChannel.isPresent());

        assertEquals(14, allocator.availableCount());
        assertEquals(1, allocator.allocatedCount());

        allocator.release(firstChannel.getAsInt());

        assertEquals(15, allocator.availableCount());
        assertEquals(0, allocator.allocatedCount());

        OptionalInt nextChannel = allocator.allocate();

        assertTrue(nextChannel.isPresent());
        assertEquals(14, allocator.availableCount());
        assertEquals(1, allocator.allocatedCount());
    }

    @Test
    void releasingInvalidChannelDoesNothing() {
        ChannelAllocator allocator = new ChannelAllocator();

        allocator.release(-1);
        allocator.release(16);
        allocator.release(ChannelAllocator.PERCUSSION_CHANNEL);

        assertEquals(15, allocator.availableCount());
        assertEquals(0, allocator.allocatedCount());
    }

    @Test
    void resetRestoresAllPitchedChannels() {
        ChannelAllocator allocator = new ChannelAllocator();

        allocator.allocate();
        allocator.allocate();

        assertEquals(13, allocator.availableCount());
        assertEquals(2, allocator.allocatedCount());

        allocator.reset();

        assertEquals(15, allocator.availableCount());
        assertEquals(0, allocator.allocatedCount());
    }

    @Test
    void recognizesUsablePitchedChannels() {
        assertTrue(ChannelAllocator.isUsablePitchedChannel(0));
        assertTrue(ChannelAllocator.isUsablePitchedChannel(8));
        assertTrue(ChannelAllocator.isUsablePitchedChannel(10));
        assertTrue(ChannelAllocator.isUsablePitchedChannel(15));

        assertFalse(ChannelAllocator.isUsablePitchedChannel(-1));
        assertFalse(ChannelAllocator.isUsablePitchedChannel(9));
        assertFalse(ChannelAllocator.isUsablePitchedChannel(16));
    }
}