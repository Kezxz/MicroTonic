package com.kezxz.microtonic.tuning;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.tuning.strategies.JustIntonationChromaticStrategy;
import com.kezxz.microtonic.tuning.strategies.MeantoneStrategy;
import com.kezxz.microtonic.tuning.strategies.NTETStrategy;
import com.kezxz.microtonic.tuning.strategies.PythagoreanStrategy;
import com.kezxz.microtonic.tuning.strategies.TwelveTETStrategy;
import com.kezxz.microtonic.util.MusicMath;

// connects the app's current settings to the active tuning strategy
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

    // resolves a logical note index into a tuned note
    public TunedNote resolve(int noteIndex) {
        TuningContext context = createContextFromAppState();
        TuningStrategy strategy = selectStrategy();

        return strategy.resolve(noteIndex, context);
    }

    // creates a TuningContext from current UI/app settings
    // though this can be used on a MIDI controller, it's mainly meant for computer keyboard input
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

    private TuningStrategy selectStrategy() {
        TuningSystem tuningSystem = TuningSystem.fromDisplayName(appState.getTuningSystem());

        return switch (tuningSystem) {
            case TWELVE_TET -> twelveTETStrategy;
            case N_TET -> nTetStrategy;
            case JUST_INTONATION -> justIntonationStrategy;
            case PYTHAGOREAN -> pythagoreanStrategy;
            case MEANTONE -> meantoneStrategy;
            default -> twelveTETStrategy;
        };
    }
}