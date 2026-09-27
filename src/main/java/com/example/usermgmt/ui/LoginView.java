package com.example.usermgmt.ui;

import com.example.usermgmt.App;
import com.example.usermgmt.model.User;
import com.example.usermgmt.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/** الشاشة الرئيسية: دخول المستخدم، دخول الأدمن، ورابط التسجيل. */
public class LoginView {

    private final App app;
    private final UserService service;
    private final VBox root = new VBox(20);

    public LoginView(App app) {
        this.app = app;
        this.service = app.getUserService();

        TabPane tabs = new TabPane(
                new Tab("دخول المستخدم", userLoginPane()),
                new Tab("دخول الأدمن", adminLoginPane()));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setMaxWidth(460);

        Hyperlink register = new Hyperlink("ليس لديك حساب؟ سجّل الآن");
        register.setOnAction(e -> app.show(new RegisterView(app).getRoot()));

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getChildren().addAll(Ui.title("نظام تسجيل وإدارة المستخدمين"), tabs, register);
    }

    private Parent userLoginPane() {
        TextField email = new TextField();
        email.setPromptText("example@mail.com");
        PasswordField password = new PasswordField();
        Label message = new Label();
        Button login = new Button("دخول");
        login.setDefaultButton(true);

        login.setOnAction(e -> {
            Optional<User> user = service.authenticate(email.getText(), password.getText());
            if (user.isEmpty()) {
                Ui.error(message, "البريد الإلكتروني أو كلمة المرور غير صحيحة");
                return;
            }
            switch (user.get().getStatus()) {
                case ACTIVE -> app.show(new UserHomeView(app, user.get()).getRoot());
                case PENDING -> Ui.error(message, "حسابك بانتظار تفعيل الأدمن");
                case DISABLED -> Ui.error(message, "حسابك معطّل، تواصل مع الإدارة");
                case BANNED -> Ui.error(message, "تم حظر حسابك ولا يمكنك الدخول");
            }
        });

        GridPane grid = Ui.form();
        grid.addRow(0, new Label("البريد الإلكتروني:"), email);
        grid.addRow(1, new Label("كلمة المرور:"), password);
        grid.add(login, 1, 2);
        grid.add(message, 0, 3, 2, 1);
        grid.setPadding(new Insets(20));
        return grid;
    }

    private Parent adminLoginPane() {
        TextField username = new TextField();
        username.setPromptText(UserService.ADMIN_USERNAME);
        PasswordField password = new PasswordField();
        Label message = new Label();
        Button login = new Button("دخول");

        login.setOnAction(e -> {
            if (service.isAdmin(username.getText(), password.getText())) {
                app.show(new AdminView(app).getRoot());
            } else {
                Ui.error(message, "بيانات الأدمن غير صحيحة");
            }
        });
        password.setOnAction(login.getOnAction());

        GridPane grid = Ui.form();
        grid.addRow(0, new Label("اسم المستخدم:"), username);
        grid.addRow(1, new Label("كلمة المرور:"), password);
        grid.add(login, 1, 2);
        grid.add(message, 0, 3, 2, 1);
        grid.setPadding(new Insets(20));
        return grid;
    }

    public Parent getRoot() {
        return root;
    }
}
