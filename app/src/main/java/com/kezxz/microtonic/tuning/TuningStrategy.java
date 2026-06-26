package com.kezxz.microtonic.tuning;

// common interface for tuning system
public interface TuningStrategy {

    String id(); // stable ID

    String displayName(); // UI label

    // converts a logical note index into a tuned note
    TunedNote resolve(int noteIndex, TuningContext context);
}