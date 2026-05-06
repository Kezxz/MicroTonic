package com.kezxz.microtonic;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.ui.MainView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX entry point for MicroTonic.
 *
 * This class should stay small. Its job is only to:
 * - create the shared application state
 * - create the main view
 * - create the JavaFX scene/window
 * - launch the app
 *
 * Most actual app logic should live elsewhere.
 */
public final class MicroTonicApp extends Application {

    /**
     * Called automatically by JavaFX when the app starts.
     *
     * The Stage is the main application window.
     */
    @Override
    public void start(Stage stage) {
        // Shared state object that stores current UI selections.
        AppState appState = new AppState();

        // MainView builds the visual controls for the app.
        MainView mainView = new MainView(appState);

        // Parent is the root JavaFX node returned by the view.
        Parent root = mainView.build();

        // The Scene contains the root visual tree and controls the window size.
        Scene scene = new Scene(root, 560, 430);

        // Load the CSS file from src/main/resources.
        scene.getStylesheets().add(
                MicroTonicApp.class.getResource("/com/kezxz/microtonic/styles.css").toExternalForm()
        );

        stage.setTitle("MicroTonic");
        stage.setScene(scene);
        stage.setMinWidth(520);
        stage.setMinHeight(400);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}