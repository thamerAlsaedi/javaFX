package com.example.usermgmt.ui;

import com.example.usermgmt.App;
import com.example.usermgmt.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** صفحة المستخدم بعد تسجيل الدخول بحساب مفعّل. */
public class UserHomeView {

    private final VBox root = new VBox(20);

    public UserHomeView(App app, User user) {
        GridPane info = Ui.form();
        info.addRow(0, new Label("الاسم:"), new Label(user.getFullName()));
        info.addRow(1, new Label("رقم الجوال:"), new Label(user.getPhone()));
        info.addRow(2, new Label("البريد الإلكتروني:"), new Label(user.getEmail()));
        info.addRow(3, new Label("العمر:"), new Label(String.valueOf(user.getAge())));
        info.addRow(4, new Label("حالة الحساب:"), new Label(user.getStatus().getArabicLabel()));

        Button logout = new Button("تسجيل الخروج");
        logout.getStyleClass().add("secondary");
        logout.setOnAction(e -> app.show(new LoginView(app).getRoot()));

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getChildren().addAll(Ui.title("مرحباً " + user.getFirstName()), info, logout);
    }

    public Parent getRoot() {
        return root;
    }
}
