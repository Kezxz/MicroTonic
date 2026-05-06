package com.kezxz.microtonic;

import com.kezxz.microtonic.app.AppState;
import com.kezxz.microtonic.ui.MainView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class MicroTonicApp extends Application {

    @Override
    public void start(Stage stage) {
        AppState appState = new AppState();
        MainView mainView = new MainView(appState);
        Parent root = mainView.build();

        Scene scene = new Scene(root, 560, 430);
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