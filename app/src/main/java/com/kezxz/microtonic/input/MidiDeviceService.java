package com.kezxz.microtonic.input;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Finds MIDI input devices connected to the computer.
 */
public final class MidiDeviceService {

    /**
     * Returns all available MIDI input devices.
     * 
     * In Java Sound terms:
     * - getMaxTransmitters() != 0 means the device can send MIDI data out to the application.
     * - getMaxTransmitters() == -1 means unlimited transmitters.
     */
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

    /**
     * Checks whether this MIDI device can act as an input source.
     */
    private boolean isInputDevice(MidiDevice.Info info) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            return device.getMaxTransmitters() != 0;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * internal/Java MIDI devices
     */

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

    /**
     * Small display-friendly record for MIDI input device metadata.
     *
     * We keep this separate from MidiDevice.Info so the UI does not depend on
     * Java Sound classes directly.
     */
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