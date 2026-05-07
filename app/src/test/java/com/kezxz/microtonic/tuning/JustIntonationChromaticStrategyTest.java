package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.util.MusicMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the 12-tone chromatic Just Intonation strategy.
 *
 * These tests focus on the most important thing:
 * note index -> correct frequency ratio.
 */
class JustIntonationChromaticStrategyTest {

    private static final double TOLERANCE = 0.000001;

    private final JustIntonationChromaticStrategy strategy = new JustIntonationChromaticStrategy();

    @Test
    void resolvesTonicAsOneToOneRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(0, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesMajorThirdAsFiveToFourRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(4, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ * (5.0 / 4.0), note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesPerfectFifthAsThreeToTwoRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(7, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ * (3.0 / 2.0), note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesNextOctaveTonicAsTwoToOneRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(12, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ * 2.0, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesPreviousOctaveTonicAsOneToTwoRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(-12, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ / 2.0, note.frequencyHz(), TOLERANCE);
    }
}