package com.kezxz.microtonic;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.app.AppPreferences;
import com.kezxz.microtonic.ui.MainView;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

// JavaFX entry point for MicroTonic
public final class MicroTonicApp extends Application {
    private MainView mainView;
    private AppPreferences appPreferences;
    private AppState appState;

    @Override
    public void start(Stage stage) {
        // load saved settings before UI binds to AppState
        appState = new AppState();
        appPreferences = new AppPreferences();
        appPreferences.loadInto(appState);

        // build the main view and register global keyboard handlers
        mainView = new MainView(appState);
        Parent root = mainView.build();

        Scene scene = new Scene(root, 560, 430);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, mainView::handleKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, mainView::handleKeyReleased);

        // load application styling from resources
        scene.getStylesheets().add(
                MicroTonicApp.class.getResource("/com/kezxz/microtonic/styles.css").toExternalForm()
        );

        stage.setTitle("MicroTonic");
        stage.setScene(scene);
        stage.setMinWidth(520);
        stage.setMinHeight(400);
        stage.setOnCloseRequest(event -> closeMainView());
        stage.show();
        stage.setAlwaysOnTop(true); // briefly force the window forward on launch
        stage.toFront();
        stage.requestFocus();
        stage.setAlwaysOnTop(false); // allow minizing
    }

    // saves settings and releases view-owned resources
    private void closeMainView() {
        if (appPreferences != null && appState != null) {
            appPreferences.save(appState);
        }

        if (mainView != null) {
            mainView.close();
        }
    }     

    @Override
    public void stop() {
        closeMainView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}