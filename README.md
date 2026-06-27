# MicroTonic

MicroTonic is a Java/JavaFX desktop app for experimenting with microtonal tuning systems.

Choose a tuning system and tonic, play notes from a computer keyboard or MIDI controller, and hear the result.

## Main Features

- JavaFX desktop interface
- Computer keyboard and MIDI controller input
- Tuning system/tonic selection
- General MIDI and direct-frequency synth playback
- Current-note and tuning feedback
- Saved settings between launches

## Tuning

This is the core purpose of the program - to allow quick experimentation with alternate tuning systems.

Currently supports:

- 12-TET
- Just Intonation
- Pythagorean
- Meantone
- N-TET

MicroTonic resolves input notes into tuned frequencies using the selected tuning strategy. For N-TET, the divisions control sets how many equal steps divide the octave.

## Sound and Input

MicroTonic has two playback engines:

**General MIDI** uses Java's built-in MIDI synthesizer. Microtonal notes are produced by choosing the nearest 12-TET MIDI note and applying pitch bend.

**Synth Waveform** plays the resolved frequency directly using a simple sampled-audio synth.

MicroTonic supports both computer keyboard input and MIDI controller input. Computer keyboard mode maps selected keys to relative scale positions ('Q' = tonic). MIDI mode maps MIDI note 60 to the selected tonic, so middle C plays the current tonic. 

## Architecture

The app follows this basic pipeline:

```text
Input → Note Index → Tuning Engine → Tuned Note → Sound Engine
```

Input events are converted into note indexes, resolved by the selected tuning strategy, and sent to the selected sound engine for playback.

## Running and Testing

Run the app:

```powershell
.\gradlew run
```
Test the app:

```powershell
.\gradlew test
```