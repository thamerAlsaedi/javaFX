package com.example.usermgmt.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/** أدوات مشتركة لبناء الواجهات. */
final class Ui {

    private Ui() {
    }

    static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("title");
        return label;
    }

    static GridPane form() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);
        return grid;
    }

    static void error(Label target, String message) {
        target.getStyleClass().setAll("label", "message-error");
        target.setText(message);
    }

    static void success(Label target, String message) {
        target.getStyleClass().setAll("label", "message-success");
        target.setText(message);
    }

    static void info(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("تنبيه");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
