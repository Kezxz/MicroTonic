package com.kezxz.microtonic.ui;

import com.kezxz.microtonic.input.MidiDeviceService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.util.function.Consumer;

// MIDI device selection and connection controls
public final class MidiSetupPane {

    private final MidiDeviceService midiDeviceService;
    private final Consumer<String> connectDevice;
    private final Runnable disconnectDevice;

    public MidiSetupPane(
            MidiDeviceService midiDeviceService,
            Consumer<String> connectDevice,
            Runnable disconnectDevice
    ) {
        this.midiDeviceService = midiDeviceService;
        this.connectDevice = connectDevice;
        this.disconnectDevice = disconnectDevice;
    }

    public TitledPane build() {
        ListView<String> midiDeviceList = new ListView<>();
        midiDeviceList.setPrefHeight(120);

        Button refreshButton = new Button("Refresh MIDI Devices");
        Button connectButton = new Button("Connect Selected MIDI Device");
        Button disconnectButton = new Button("Disconnect MIDI Device");

        Label statusLabel = new Label("Ready. Select a tuning, choose an input source, and play.");

        Runnable refreshDevices = () -> {
            var devices = midiDeviceService.listInputDevices();

            if (devices.isEmpty()) {
                midiDeviceList.setItems(FXCollections.observableArrayList("No MIDI input devices found."));
                statusLabel.setText("No user-facing MIDI controllers found. Connect one, then click Refresh MIDI Devices.");
                return;
            }

            var displayNames = devices.stream()
                    .map(MidiDeviceService.MidiInputDeviceInfo::displayName)
                    .toList();

            midiDeviceList.setItems(FXCollections.observableArrayList(displayNames));
            statusLabel.setText(devices.size() + " MIDI input device(s) found.");
        };

        refreshButton.setOnAction(event -> refreshDevices.run());

        connectButton.setOnAction(event -> {
            String selectedDevice = midiDeviceList.getSelectionModel().getSelectedItem();

            if (selectedDevice == null || selectedDevice.equals("No MIDI input devices found.")) {
                statusLabel.setText("Select a MIDI input device first.");
                return;
            }

            try {
                connectDevice.accept(selectedDevice);
                statusLabel.setText("Connected to " + selectedDevice);
            } catch (RuntimeException exception) {
                statusLabel.setText("Could not connect: " + exception.getMessage());
            }
        });

        disconnectButton.setOnAction(event -> {
            disconnectDevice.run();
            statusLabel.setText("MIDI device disconnected.");
        });

        refreshDevices.run();

        GridPane midiGrid = new GridPane();
        midiGrid.setHgap(12);
        midiGrid.setVgap(12);
        midiGrid.setPadding(new Insets(16));

        midiGrid.add(refreshButton, 0, 0);
        midiGrid.add(connectButton, 1, 0);
        midiGrid.add(disconnectButton, 2, 0);
        midiGrid.add(statusLabel, 0, 1, 3, 1);
        midiGrid.add(midiDeviceList, 0, 2, 3, 1);

        TitledPane midiDevicesPane = new TitledPane("MIDI Setup", midiGrid);
        midiDevicesPane.setCollapsible(true);
        midiDevicesPane.setExpanded(true);

        return midiDevicesPane;
    }
}