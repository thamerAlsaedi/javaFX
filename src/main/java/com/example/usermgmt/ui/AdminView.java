package com.example.usermgmt.ui;

import com.example.usermgmt.App;
import com.example.usermgmt.model.AccountStatus;
import com.example.usermgmt.model.User;
import com.example.usermgmt.service.UserService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/** لوحة الأدمن: استعراض الحسابات وتفعيلها أو تعطيلها أو حظرها. */
public class AdminView {

    private static final String ALL = "الكل";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserService service;
    private final BorderPane root = new BorderPane();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final FilteredList<User> filtered = new FilteredList<>(users);
    private final TableView<User> table = new TableView<>(filtered);
    private final TextField search = new TextField();
    private final ComboBox<Object> statusFilter = new ComboBox<>();
    private final Label stats = new Label();

    public AdminView(App app) {
        this.service = app.getUserService();

        buildTable();

        search.setPromptText("بحث بالاسم أو الجوال أو البريد");
        HBox.setHgrow(search, Priority.ALWAYS);
        statusFilter.getItems().add(ALL);
        statusFilter.getItems().addAll((Object[]) AccountStatus.values());
        statusFilter.setValue(ALL);
        search.textProperty().addListener((obs, o, n) -> applyFilter());
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilter());

        Button logout = new Button("تسجيل الخروج");
        logout.getStyleClass().add("secondary");
        logout.setOnAction(e -> app.show(new LoginView(app).getRoot()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, Ui.title("لوحة تحكم الأدمن"), spacer, logout);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox filters = new HBox(10, new Label("الحالة:"), statusFilter, search);
        filters.setAlignment(Pos.CENTER_LEFT);

        Button activate = actionButton("تفعيل", "activate", AccountStatus.ACTIVE);
        Button disable = actionButton("تعطيل", "disable", AccountStatus.DISABLED);
        Button ban = actionButton("حظر", "ban", AccountStatus.BANNED);
        Button delete = new Button("حذف");
        delete.getStyleClass().add("ban");
        delete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        delete.setOnAction(e -> deleteSelected());

        HBox actions = new HBox(10, activate, disable, ban, delete, new Region(), stats);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(15, header, filters);
        top.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(top);
        root.setCenter(table);
        BorderPane.setMargin(actions, new Insets(15, 0, 0, 0));
        root.setBottom(actions);
        root.setPadding(new Insets(20));

        refresh();
    }

    private void buildTable() {
        TableColumn<User, Number> id = new TableColumn<>("#");
        id.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        id.setPrefWidth(45);

        TableColumn<User, String> first = column("الاسم الأول", u -> u.getFirstName(), 110);
        TableColumn<User, String> last = column("الاسم الثاني", u -> u.getLastName(), 110);
        TableColumn<User, String> phone = column("رقم الجوال", u -> u.getPhone(), 110);
        TableColumn<User, String> email = column("البريد الإلكتروني", u -> u.getEmail(), 190);
        TableColumn<User, String> age = column("العمر", u -> String.valueOf(u.getAge()), 60);
        TableColumn<User, String> created = column("تاريخ التسجيل",
                u -> u.getCreatedAt().format(DATE), 130);

        TableColumn<User, AccountStatus> status = new TableColumn<>("الحالة");
        status.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStatus()));
        status.setPrefWidth(120);
        status.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(AccountStatus item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeIf(s -> s.startsWith("status-"));
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getArabicLabel());
                    getStyleClass().add("status-" + item.name().toLowerCase());
                }
            }
        });

        table.getColumns().addAll(java.util.List.of(id, first, last, phone, email, age, created, status));
        table.setPlaceholder(new Label("لا توجد حسابات"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private static TableColumn<User, String> column(String title,
                                                    java.util.function.Function<User, String> getter,
                                                    double width) {
        TableColumn<User, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private Button actionButton(String text, String styleClass, AccountStatus target) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        // يُعطَّل الزر عند عدم اختيار حساب أو إذا كان الحساب بالحالة المطلوبة أصلاً
        button.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> {
                    User u = table.getSelectionModel().getSelectedItem();
                    return u == null || u.getStatus() == target;
                },
                table.getSelectionModel().selectedItemProperty(), users));
        button.setOnAction(e -> changeStatus(target));
        return button;
    }

    private void changeStatus(AccountStatus target) {
        User user = table.getSelectionModel().getSelectedItem();
        if (user == null) {
            return;
        }
        if (target == AccountStatus.BANNED && !confirm("حظر الحساب",
                "هل تريد حظر حساب " + user.getFullName() + "؟ لن يتمكن من تسجيل الدخول.")) {
            return;
        }
        service.changeStatus(user, target);
        refresh();
        table.getSelectionModel().select(user);
    }

    private void deleteSelected() {
        User user = table.getSelectionModel().getSelectedItem();
        if (user != null && confirm("حذف الحساب",
                "هل تريد حذف حساب " + user.getFullName() + " نهائياً؟")) {
            service.delete(user);
            refresh();
        }
    }

    private boolean confirm(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle("تأكيد");
        alert.setHeaderText(header);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void refresh() {
        users.setAll(service.getAllUsers());
        table.refresh();
        applyFilter();
    }

    private void applyFilter() {
        String query = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        Object selected = statusFilter.getValue();
        filtered.setPredicate(u -> {
            boolean statusOk = selected == null || ALL.equals(selected) || u.getStatus() == selected;
            boolean queryOk = query.isEmpty()
                    || u.getFullName().toLowerCase().contains(query)
                    || u.getPhone().contains(query)
                    || u.getEmail().contains(query);
            return statusOk && queryOk;
        });
        updateStats();
    }

    private void updateStats() {
        StringBuilder sb = new StringBuilder("الإجمالي: ").append(users.size());
        for (AccountStatus s : AccountStatus.values()) {
            long count = users.stream().filter(u -> u.getStatus() == s).count();
            sb.append("  |  ").append(s.getArabicLabel()).append(": ").append(count);
        }
        stats.setText(sb.toString());
    }

    public Parent getRoot() {
        return root;
    }
}
