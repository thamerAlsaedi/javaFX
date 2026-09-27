package com.example.usermgmt;

import com.example.usermgmt.service.UserService;
import com.example.usermgmt.ui.LoginView;
import javafx.application.Application;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

/** نقطة تشغيل البرنامج وإدارة التنقل بين الشاشات. */
public class App extends Application {

    private Stage stage;
    private UserService userService;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.userService = new UserService(Path.of("data", "users.dat"));
        stage.setTitle("نظام تسجيل وإدارة المستخدمين");
        stage.setScene(new Scene(new LoginView(this).getRoot(), 950, 620));
        applyStyle(stage.getScene());
        stage.show();
    }

    /** يستبدل محتوى النافذة بالشاشة المعطاة. */
    public void show(Parent root) {
        stage.getScene().setRoot(root);
    }

    public UserService getUserService() {
        return userService;
    }

    private static void applyStyle(Scene scene) {
        scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        scene.getStylesheets().add(App.class.getResource("/styles.css").toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
