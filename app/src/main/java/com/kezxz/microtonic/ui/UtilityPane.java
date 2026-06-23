package com.kezxz.microtonic.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

// utility actions for recovering from playback/input issues
public final class UtilityPane {

    private final Runnable panicAction;

    public UtilityPane(Runnable panicAction) {
        this.panicAction = panicAction;
    }

    public TitledPane build() {
        GridPane utilityGrid = new GridPane();
        utilityGrid.setHgap(12);
        utilityGrid.setVgap(12);
        utilityGrid.setPadding(new Insets(16));

        Button panicButton = new Button("Panic / All Notes Off");
        panicButton.setOnAction(event -> panicAction.run());

        Label helpLabel = new Label("Use this if notes get stuck or MIDI behaves unexpectedly.");

        utilityGrid.add(panicButton, 0, 0);
        utilityGrid.add(helpLabel, 1, 0);

        TitledPane utilityPane = new TitledPane("Utilities", utilityGrid);
        utilityPane.setCollapsible(true);
        utilityPane.setExpanded(false);

        return utilityPane;
    }
}