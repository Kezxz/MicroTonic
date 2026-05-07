package com.kezxz.microtonic.tuning;

/**
 * Strategy interface for tuning systems.
 *
 * Each tuning system answers the same core question:
 *
 * Given:
 * - a note index
 * - a tuning context
 *
 * Return:
 * - the exact frequency
 * - nearest MIDI note
 * - cents deviation
 * - display label
 *
 * This lets the rest of the app use any tuning system without caring how the tuning math works internally.
 */
public interface TuningStrategy {

    String id(); // stable machine-readable ID: useful for presets, config files, or saving settings

    String displayName(); // readable name for UI display

    /**
     * Converts a logical note index into a tuned note.
     *
     * noteIndex is intentionally abstract:
     * - in keyboard mode, it may mean key layout index
     * - in MIDI mode, it may mean offset from a reference MIDI note
     */
    TunedNote resolve(int noteIndex, TuningContext context);
}