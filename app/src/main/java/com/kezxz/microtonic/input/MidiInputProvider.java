package com.kezxz.microtonic.input;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

// opens MIDI input devices and listens for note-on / note-off messages
public final class MidiInputProvider implements AutoCloseable {

    // MIDI note 60 maps to noteIndex 0, so middle C plays selected tonic
    public static final int REFERENCE_MIDI_NOTE = 60;

    private MidiDevice openDevice;
    private Transmitter transmitter;
    private Receiver receiver;

    public void openByDisplayName(String displayName, MidiNoteListener listener) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("MIDI device display name cannot be blank.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("MIDI note listener cannot be null.");
        }

        close();

        MidiDevice.Info matchingInfo = findInputDeviceInfo(displayName);

        try {
            openDevice = MidiSystem.getMidiDevice(matchingInfo);
            openDevice.open();

            receiver = new MidiInputReceiver(listener);
            transmitter = openDevice.getTransmitter();
            transmitter.setReceiver(receiver);
        } catch (MidiUnavailableException exception) {
            close();
            throw new IllegalStateException("Unable to open MIDI input device: " + displayName, exception);
        }
    }

    public boolean isOpen() {
        return openDevice != null && openDevice.isOpen();
    }

    @Override
    public void close() {
        if (transmitter != null) {
            transmitter.close();
            transmitter = null;
        }

        if (receiver != null) {
            receiver.close();
            receiver = null;
        }

        if (openDevice != null && openDevice.isOpen()) {
            openDevice.close();
        }

        openDevice = null;
    }

    private MidiDevice.Info findInputDeviceInfo(String displayName) {
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (isInputDevice(info) && displayNameFor(info).equals(displayName)) {
                return info;
            }
        }

        throw new IllegalArgumentException("MIDI input device not found: " + displayName);
    }

    private boolean isInputDevice(MidiDevice.Info info) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            return device.getMaxTransmitters() != 0;
        } catch (Exception exception) {
            return false;
        }
    }

    private String displayNameFor(MidiDevice.Info info) {
        String name = clean(info.getName());
        String vendor = clean(info.getVendor());

        if (vendor.isBlank()) {
            return name;
        }

        return name + " — " + vendor;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class MidiInputReceiver implements Receiver {

        private final MidiNoteListener listener;
        private boolean closed;

        private MidiInputReceiver(MidiNoteListener listener) {
            this.listener = listener;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (closed || !(message instanceof ShortMessage shortMessage)) {
                return;
            }

            int command = shortMessage.getCommand();
            int midiNote = shortMessage.getData1();
            int velocity = shortMessage.getData2();

            if (command == ShortMessage.NOTE_ON && velocity > 0) {
                listener.noteOn(midiNote, velocity);
                return;
            }

            // MIDI convention: NOTE_ON with velocity 0 should be treated as NOTE_OFF.
            if (command == ShortMessage.NOTE_OFF || command == ShortMessage.NOTE_ON) {
                listener.noteOff(midiNote);
            }
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    public interface MidiNoteListener {
        void noteOn(int midiNote, int velocity);

        void noteOff(int midiNote);
    }
}