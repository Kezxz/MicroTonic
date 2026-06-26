package com.kezxz.microtonic.tuning.strategies;

import com.kezxz.microtonic.tuning.TunedNote;
import com.kezxz.microtonic.tuning.TuningContext;
import com.kezxz.microtonic.util.MusicMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PythagoreanStrategyTest {

    private static final double TOLERANCE = 0.000001;

    private final PythagoreanStrategy strategy = new PythagoreanStrategy();

    @Test
    void resolvesTonicAsOneToOneRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(0, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ, note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesMajorSecondAsNineToEightRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(2, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ * (9.0 / 8.0), note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesMajorThirdAsEightyOneToSixtyFourRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(4, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ * (81.0 / 64.0), note.frequencyHz(), TOLERANCE);
    }

    @Test
    void resolvesPerfectFourthAsFourToThreeRatio() {
        TuningContext context = TuningContext.defaultContext();

        TunedNote note = strategy.resolve(5, context);

        assertEquals(MusicMath.C4_FREQUENCY_HZ * (4.0 / 3.0), note.frequencyHz(), TOLERANCE);
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