package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.util.MusicMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeantoneStrategyTest {

    private static final double TOLERANCE = 0.000001;

    private final MeantoneStrategy strategy = new MeantoneStrategy();

    @Test
    void resolvesTonicAsZeroCents() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(0, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesMajorSecondAsExpectedMeantoneCents() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(2, context);

        double expectedFrequency = MusicMath.C4_FREQUENCY_HZ * MusicMath.centsToRatio(193.157);
        assertEquals(expectedFrequency, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesMajorThirdAsExpectedMeantoneCents() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(4, context);

        double expectedFrequency = MusicMath.C4_FREQUENCY_HZ * MusicMath.centsToRatio(386.314);
        assertEquals(expectedFrequency, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesPerfectFifthAsExpectedMeantoneCents() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(7, context);

        double expectedFrequency = MusicMath.C4_FREQUENCY_HZ * MusicMath.centsToRatio(696.578);
        assertEquals(expectedFrequency, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesNextOctaveTonicAsTwelveHundredCents() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(12, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ * 2.0, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesPreviousOctaveTonicAsNegativeTwelveHundredCents() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(-12, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ / 2.0, note.frequencyHz(), TOLERANCE);
    }
}