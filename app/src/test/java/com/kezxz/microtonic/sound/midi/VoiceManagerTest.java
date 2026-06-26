package com.kezxz.microtonic.sound.midi;

import com.kezxz.microtonic.tuning.TunedNote;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VoiceManagerTest {

    private static final TunedNote C4 = new TunedNote(
            0,
            261.6255653005986,
            60,
            0.0,
            "C4 test note"
    );

    @Test
    void startsVoiceOnAvailableChannel() {
        ChannelAllocator allocator = new ChannelAllocator();
        VoiceManager manager = new VoiceManager(allocator);

        VoiceManager.VoiceAllocation allocation = manager.startVoice(60, 0, C4, 100);

        assertEquals(1, manager.activeVoiceCount());
        assertEquals(1, allocator.allocatedCount());
        assertTrue(allocator.isAllocated(allocation.newVoice().channel()));
    }

    @Test
    void releasesVoiceAndFreesChannel() {
        ChannelAllocator allocator = new ChannelAllocator();
        VoiceManager manager = new VoiceManager(allocator);

        VoiceManager.VoiceAllocation allocation = manager.startVoice(60, 0, C4, 100);

        Optional<Voice> releasedVoice = manager.releaseVoice(60);

        assertTrue(releasedVoice.isPresent());
        assertEquals(allocation.newVoice(), releasedVoice.get());
        assertEquals(0, manager.activeVoiceCount());
        assertEquals(0, allocator.allocatedCount());
    }

    @Test
    void releaseUnknownVoiceDoesNothing() {
        ChannelAllocator allocator = new ChannelAllocator();
        VoiceManager manager = new VoiceManager(allocator);

        Optional<Voice> releasedVoice = manager.releaseVoice(999);

        assertTrue(releasedVoice.isEmpty());
        assertEquals(0, manager.activeVoiceCount());
        assertEquals(0, allocator.allocatedCount());
    }

    @Test
    void repeatedInputNoteReplacesExistingVoice() {
        ChannelAllocator allocator = new ChannelAllocator();
        VoiceManager manager = new VoiceManager(allocator);

        VoiceManager.VoiceAllocation firstAllocation = manager.startVoice(60, 0, C4, 100);
        VoiceManager.VoiceAllocation secondAllocation = manager.startVoice(60, 0, C4, 100);

        assertTrue(secondAllocation.replacedExistingVoice());
        assertEquals(firstAllocation.newVoice(), secondAllocation.replacedVoice());
        assertEquals(1, manager.activeVoiceCount());
        assertEquals(1, allocator.allocatedCount());
    }

    @Test
    void releaseAllClearsVoicesAndChannels() {
        ChannelAllocator allocator = new ChannelAllocator();
        VoiceManager manager = new VoiceManager(allocator);

        manager.startVoice(60, 0, C4, 100);
        manager.startVoice(64, 4, C4, 100);

        assertEquals(2, manager.activeVoiceCount());
        assertEquals(2, allocator.allocatedCount());

        manager.releaseAll();

        assertEquals(0, manager.activeVoiceCount());
        assertEquals(0, allocator.allocatedCount());
        assertEquals(15, allocator.availableCount());
    }

    @Test
    void stealsOldestVoiceWhenAllChannelsAreUsed() {
        ChannelAllocator allocator = new ChannelAllocator();
        VoiceManager manager = new VoiceManager(allocator);

        for (int i = 0; i < 15; i++) {
            manager.startVoice(60 + i, i, C4, 100);
        }

        assertEquals(15, manager.activeVoiceCount());
        assertEquals(15, allocator.allocatedCount());

        VoiceManager.VoiceAllocation allocation = manager.startVoice(100, 100, C4, 100);

        assertTrue(allocation.stoleVoice());
        assertTrue(allocation.stolenInputNoteId().isPresent());
        assertEquals(60, allocation.stolenInputNoteId().get());
        assertEquals(15, manager.activeVoiceCount());
        assertEquals(15, allocator.allocatedCount());
        assertTrue(manager.findVoice(60).isEmpty());
        assertTrue(manager.findVoice(100).isPresent());
    }
}