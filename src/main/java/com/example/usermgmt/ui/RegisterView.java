package com.example.usermgmt.ui;

import com.example.usermgmt.App;
import com.example.usermgmt.service.ValidationException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** شاشة تسجيل مستخدم جديد. */
public class RegisterView {

    private final VBox root = new VBox(20);

    public RegisterView(App app) {
        TextField firstName = new TextField();
        TextField lastName = new TextField();
        TextField phone = new TextField();
        phone.setPromptText("05xxxxxxxx");
        TextField email = new TextField();
        email.setPromptText("example@mail.com");
        TextField age = new TextField();
        PasswordField password = new PasswordField();
        PasswordField confirm = new PasswordField();
        Label message = new Label();
        message.setWrapText(true);
        message.setMaxWidth(420);

        Button register = new Button("تسجيل");
        register.setDefaultButton(true);
        Button back = new Button("رجوع");
        back.getStyleClass().add("secondary");
        back.setOnAction(e -> app.show(new LoginView(app).getRoot()));

        register.setOnAction(e -> {
            if (!password.getText().equals(confirm.getText())) {
                Ui.error(message, "كلمتا المرور غير متطابقتين");
                return;
            }
            try {
                app.getUserService().register(firstName.getText(), lastName.getText(),
                        phone.getText(), email.getText(), age.getText(), password.getText());
                Ui.info("تم التسجيل بنجاح",
                        "تم إنشاء حسابك وهو الآن بانتظار تفعيل الأدمن.");
                app.show(new LoginView(app).getRoot());
            } catch (ValidationException ex) {
                Ui.error(message, ex.getMessage());
            }
        });

        GridPane grid = Ui.form();
        grid.addRow(0, new Label("الاسم الأول:"), firstName);
        grid.addRow(1, new Label("الاسم الثاني:"), lastName);
        grid.addRow(2, new Label("رقم الجوال:"), phone);
        grid.addRow(3, new Label("البريد الإلكتروني:"), email);
        grid.addRow(4, new Label("العمر:"), age);
        grid.addRow(5, new Label("كلمة المرور:"), password);
        grid.addRow(6, new Label("تأكيد كلمة المرور:"), confirm);

        HBox buttons = new HBox(10, register, back);
        buttons.setAlignment(Pos.CENTER);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getChildren().addAll(Ui.title("تسجيل حساب جديد"), grid, buttons, message);
    }

    public Parent getRoot() {
        return root;
    }
}
