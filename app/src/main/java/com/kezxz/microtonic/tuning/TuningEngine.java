package com.kezxz.microtonic.tuning;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.tuning.strategies.NTETStrategy;
import com.kezxz.microtonic.tuning.strategies.JustIntonationChromaticStrategy;
import com.kezxz.microtonic.tuning.strategies.MeantoneStrategy;
import com.kezxz.microtonic.tuning.strategies.PythagoreanStrategy;
import com.kezxz.microtonic.tuning.strategies.TwelveTETStrategy;
import com.kezxz.microtonic.util.MusicMath;

/**
 * Central tuning coordinator.
 *
 * This class connects the app's current settings to the correct tuning strategy.
 *
 * The rest of the app should eventually ask this class:
 *
 * "Given this note index, what exact tuned note should I play?"
 *
 * This keeps UI code and sound code from needing to know details about each
 * individual tuning system.
 */
public final class TuningEngine {

    private final AppState appState;

    private final TwelveTETStrategy twelveTETStrategy = new TwelveTETStrategy();
    private final NTETStrategy nTetStrategy = new NTETStrategy();
    private final JustIntonationChromaticStrategy justIntonationStrategy = new JustIntonationChromaticStrategy();
    private final PythagoreanStrategy pythagoreanStrategy = new PythagoreanStrategy();
    private final MeantoneStrategy meantoneStrategy = new MeantoneStrategy();

    public TuningEngine(AppState appState) {
        this.appState = appState;
    }

    /**
     * Resolves a logical note index into a tuned note.
     *
     * Example:
     * - noteIndex 0 means the selected tonic
     * - noteIndex 1 means the next step in the active tuning system
     * - noteIndex 12 often means one octave above the tonic, depending on tuning
     */
    public TunedNote resolve(int noteIndex) {
        TuningContext context = createContextFromAppState();
        TuningStrategy strategy = selectStrategy();

        return strategy.resolve(noteIndex, context);
    }

    /**
     * Creates a TuningContext from current UI/app settings.
     *
     * The tonic frequency is currently anchored around C4.
     *
     * Example:
     * - C tonic = C4
     * - D tonic = D4
     * - A tonic = A4-ish relative to C4's octave
     */
    private TuningContext createContextFromAppState() {
        PitchClass tonic = PitchClass.fromDisplayName(appState.getTonic());

        double tonicFrequencyHz = MusicMath.C4_FREQUENCY_HZ
                * Math.pow(2.0, tonic.semitoneOffsetFromC() / 12.0);

        return new TuningContext(
                tonic,
                tonicFrequencyHz,
                appState.getNTetDivisions()
        );
    }

    /**
     * Chooses a tuning strategy from the current tuning-system dropdown value.
     */
    private TuningStrategy selectStrategy() {
        TuningSystem tuningSystem = TuningSystem.fromDisplayName(appState.getTuningSystem());

        return switch (tuningSystem) {
            case TWELVE_TET -> twelveTETStrategy;
            case N_TET -> nTetStrategy;
            case JUST_INTONATION_CHROMATIC -> justIntonationStrategy;
            case PYTHAGOREAN -> pythagoreanStrategy;
            case MEANTONE -> meantoneStrategy;
            default -> twelveTETStrategy;
        };
    }
}