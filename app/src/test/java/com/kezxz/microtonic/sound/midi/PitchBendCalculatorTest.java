package com.kezxz.microtonic.sound.midi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for converting cents offsets into MIDI pitch bend values.
 */
class PitchBendCalculatorTest {

    @Test
    void zeroCentsMapsToCenterPitchBend() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(0.0);

        assertEquals(PitchBendCalculator.CENTER_PITCH_BEND, pitchBend);
    }

    @Test
    void maximumDownwardBendMapsToMinimumPitchBend() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(-200.0);

        assertEquals(PitchBendCalculator.MIN_PITCH_BEND, pitchBend);
    }

    @Test
    void maximumUpwardBendMapsNearMaximumPitchBend() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(200.0);

        assertEquals(PitchBendCalculator.MAX_PITCH_BEND, pitchBend);
    }

    @Test
    void halfwayUpwardBendMapsHalfwayAboveCenter() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(100.0);

        assertEquals(12288, pitchBend);
    }

    @Test
    void halfwayDownwardBendMapsHalfwayBelowCenter() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(-100.0);

        assertEquals(4096, pitchBend);
    }

    @Test
    void valuesAboveMaximumAreClamped() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(300.0);

        assertEquals(PitchBendCalculator.MAX_PITCH_BEND, pitchBend);
    }

    @Test
    void valuesBelowMinimumAreClamped() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(-300.0);

        assertEquals(PitchBendCalculator.MIN_PITCH_BEND, pitchBend);
    }

    @Test
    void customBendRangeCanBeUsed() {
        int pitchBend = PitchBendCalculator.centsToPitchBend(50.0, 100.0);

        assertEquals(12288, pitchBend);
    }

    @Test
    void rejectsInvalidBendRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PitchBendCalculator.centsToPitchBend(0.0, 0.0)
        );
    }
}