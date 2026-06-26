package com.kezxz.microtonic.input;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// finds MIDI devices than can be shown as input options
public final class MidiDeviceService {

    // returns all available MIDI input devices
    public List<MidiInputDeviceInfo> listInputDevices() {
        List<MidiInputDeviceInfo> devices = new ArrayList<>();

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (isInputDevice(info) && isUserFacingInputDevice(info)) {
                devices.add(MidiInputDeviceInfo.from(info));
            }
        }

        devices.sort(Comparator.comparing(MidiInputDeviceInfo::displayName));

        return devices;
    }

    private boolean isInputDevice(MidiDevice.Info info) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            return device.getMaxTransmitters() != 0;
        } catch (Exception exception) {
            return false;
        }
    }

    // internal/Java MIDI devices
    private boolean isUserFacingInputDevice(MidiDevice.Info info) {
        String name = clean(info.getName()).toLowerCase();
        String description = clean(info.getDescription()).toLowerCase();

        if (name.contains("real time sequencer")) {
            return false;
        }

        if (name.contains("java sound synthesizer")) {
            return false;
        }

        if (description.contains("software sequencer")) {
            return false;
        }

        return true;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    // lists user-facing MIDI input devices
    public record MidiInputDeviceInfo(
            String name,
            String vendor,
            String description,
            String version
    ) {
        public static MidiInputDeviceInfo from(MidiDevice.Info info) {
            return new MidiInputDeviceInfo(
                    clean(info.getName()),
                    clean(info.getVendor()),
                    clean(info.getDescription()),
                    clean(info.getVersion())
            );
        }

        public String displayName() {
            if (vendor.isBlank()) {
                return name;
            }

            return name + " — " + vendor;
        }

        private static String clean(String value) {
            return value == null ? "" : value.trim();
        }
    }
}