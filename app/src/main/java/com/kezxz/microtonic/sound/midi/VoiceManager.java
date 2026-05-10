package com.kezxz.microtonic.sound.midi;

import com.kezxz.microtonic.tuning.TunedNote;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tracks active MIDI voices.
 *
 * This class does not send MIDI messages itself.
 * Its job is only to decide:
 * - which channel a new note gets
 * - which voice belongs to a note-off event
 * - which old voice should be stolen if all channels are full
 */
public final class VoiceManager {

    private final ChannelAllocator channelAllocator;
    private final Map<Integer, Voice> activeVoicesByInputNoteId = new HashMap<>();

    private long nextVoiceId = 0;

    public VoiceManager(ChannelAllocator channelAllocator) {
        if (channelAllocator == null) {
            throw new IllegalArgumentException("Channel allocator cannot be null.");
        }

        this.channelAllocator = channelAllocator;
    }

    /**
     * Starts tracking a voice for a newly pressed note.
     *
     * If this input note is already active, the old voice is released first.
     * This prevents duplicate stuck voices for repeated note-on events.
     */
    public VoiceAllocation startVoice(
            int inputNoteId,
            int noteIndex,
            TunedNote tunedNote,
            int velocity
    ) {
        if (tunedNote == null) {
            throw new IllegalArgumentException("Tuned note cannot be null.");
        }

        if (velocity < 0 || velocity > 127) {
            throw new IllegalArgumentException("Velocity must be between 0 and 127.");
        }

        Voice replacedVoice = activeVoicesByInputNoteId.remove(inputNoteId);
        if (replacedVoice != null) {
            channelAllocator.release(replacedVoice.channel());
        }

        Optional<Integer> stolenInputNoteId = Optional.empty();
        Voice stolenVoice = null;

        Optional<Integer> allocatedChannel = channelAllocator.allocate().stream().boxed().findFirst();

        if (allocatedChannel.isEmpty()) {
            stolenVoice = findOldestVoice()
                    .orElseThrow(() -> new IllegalStateException("No channel available and no voice to steal."));

            activeVoicesByInputNoteId.remove(stolenVoice.inputNoteId());
            channelAllocator.release(stolenVoice.channel());

            allocatedChannel = channelAllocator.allocate().stream().boxed().findFirst();

            if (allocatedChannel.isEmpty()) {
                throw new IllegalStateException("Unable to allocate channel after stealing voice.");
            }

            stolenInputNoteId = Optional.of(stolenVoice.inputNoteId());
        }

        Voice newVoice = new Voice(
                nextVoiceId++,
                inputNoteId,
                noteIndex,
                tunedNote.nearestMidiNote(),
                allocatedChannel.get(),
                tunedNote.frequencyHz(),
                velocity,
                Instant.now()
        );

        activeVoicesByInputNoteId.put(inputNoteId, newVoice);

        return new VoiceAllocation(newVoice, replacedVoice, stolenVoice, stolenInputNoteId);
    }

    /**
     * Stops tracking the voice for a released note.
     *
     * Returns Optional.empty() if the input note was not active.
     */
    public Optional<Voice> releaseVoice(int inputNoteId) {
        Voice voice = activeVoicesByInputNoteId.remove(inputNoteId);

        if (voice == null) {
            return Optional.empty();
        }

        channelAllocator.release(voice.channel());
        return Optional.of(voice);
    }

    /**
     * Releases every active voice and resets the allocator.
     *
     * Useful for panic/all-notes-off behavior.
     */
    public void releaseAll() {
        activeVoicesByInputNoteId.clear();
        channelAllocator.reset();
    }

    public Optional<Voice> findVoice(int inputNoteId) {
        return Optional.ofNullable(activeVoicesByInputNoteId.get(inputNoteId));
    }

    public int activeVoiceCount() {
        return activeVoicesByInputNoteId.size();
    }

    private Optional<Voice> findOldestVoice() {
        return activeVoicesByInputNoteId.values()
                .stream()
                .min(Comparator.comparingLong(Voice::voiceId));
    }

    /**
     * Result of starting a voice.
     *
     * The future MIDI sound engine needs to know:
     * - which new voice to play
     * - whether an older voice was replaced
     * - whether an older voice was stolen due to full polyphony
     */
    public record VoiceAllocation(
            Voice newVoice,
            Voice replacedVoice,
            Voice stolenVoice,
            Optional<Integer> stolenInputNoteId
    ) {
        public VoiceAllocation {
            if (newVoice == null) {
                throw new IllegalArgumentException("New voice cannot be null.");
            }

            if (stolenInputNoteId == null) {
                throw new IllegalArgumentException("Stolen input note ID optional cannot be null.");
            }
        }

        public boolean replacedExistingVoice() {
            return replacedVoice != null;
        }

        public boolean stoleVoice() {
            return stolenVoice != null;
        }
    }
}