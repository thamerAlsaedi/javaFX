package com.example.usermgmt.service;

import com.example.usermgmt.model.AccountStatus;
import com.example.usermgmt.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * منطق التسجيل وتسجيل الدخول وإدارة الحسابات.
 * تُحفظ البيانات في ملف حتى تبقى بعد إغلاق البرنامج.
 */
public class UserService {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    private static final Pattern NAME = Pattern.compile("^[\\p{L} ]{2,30}$");
    private static final Pattern PHONE = Pattern.compile("^05\\d{8}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final Path storageFile;
    private final List<User> users = new ArrayList<>();
    private long nextId = 1;

    public UserService(Path storageFile) {
        this.storageFile = storageFile;
        load();
    }

    /** تسجيل مستخدم جديد؛ يبدأ الحساب بحالة "بانتظار التفعيل". */
    public synchronized User register(String firstName, String lastName, String phone,
                                      String email, String ageText, String password)
            throws ValidationException {
        firstName = trim(firstName);
        lastName = trim(lastName);
        phone = trim(phone);
        email = trim(email).toLowerCase();
        ageText = trim(ageText);

        if (!NAME.matcher(firstName).matches()) {
            throw new ValidationException("الاسم الأول مطلوب ويجب أن يحتوي على حروف فقط (2-30 حرفاً)");
        }
        if (!NAME.matcher(lastName).matches()) {
            throw new ValidationException("الاسم الثاني مطلوب ويجب أن يحتوي على حروف فقط (2-30 حرفاً)");
        }
        if (!PHONE.matcher(phone).matches()) {
            throw new ValidationException("رقم الجوال يجب أن يبدأ بـ 05 ويتكون من 10 أرقام");
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("البريد الإلكتروني غير صالح");
        }
        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            throw new ValidationException("العمر يجب أن يكون رقماً");
        }
        if (age < 10 || age > 120) {
            throw new ValidationException("العمر يجب أن يكون بين 10 و 120");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("كلمة المرور يجب ألا تقل عن 6 أحرف");
        }
        String finalEmail = email;
        String finalPhone = phone;
        if (users.stream().anyMatch(u -> u.getEmail().equals(finalEmail))) {
            throw new ValidationException("البريد الإلكتروني مسجّل مسبقاً");
        }
        if (users.stream().anyMatch(u -> u.getPhone().equals(finalPhone))) {
            throw new ValidationException("رقم الجوال مسجّل مسبقاً");
        }

        User user = new User(nextId++, firstName, lastName, phone, email, age, hash(password));
        users.add(user);
        save();
        return user;
    }

    /** يعيد المستخدم إذا تطابق البريد وكلمة المرور، بغض النظر عن حالته. */
    public synchronized Optional<User> authenticate(String email, String password) {
        String normalized = trim(email).toLowerCase();
        String hashed = hash(password == null ? "" : password);
        return users.stream()
                .filter(u -> u.getEmail().equals(normalized) && u.getPasswordHash().equals(hashed))
                .findFirst();
    }

    public boolean isAdmin(String username, String password) {
        return ADMIN_USERNAME.equals(trim(username)) && ADMIN_PASSWORD.equals(password);
    }

    public synchronized List<User> getAllUsers() {
        return List.copyOf(users);
    }

    public synchronized void changeStatus(User user, AccountStatus status) {
        user.setStatus(status);
        save();
    }

    public synchronized void delete(User user) {
        users.removeIf(u -> u.getId() == user.getId());
        save();
    }

    private void load() {
        if (!Files.exists(storageFile)) {
            return;
        }
        try (InputStream in = Files.newInputStream(storageFile);
             ObjectInputStream ois = new ObjectInputStream(in)) {
            @SuppressWarnings("unchecked")
            List<User> loaded = (List<User>) ois.readObject();
            users.addAll(loaded);
            nextId = users.stream().mapToLong(User::getId).max().orElse(0) + 1;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("تعذّر قراءة ملف البيانات: " + storageFile, e);
        }
    }

    private void save() {
        try {
            Path parent = storageFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(storageFile);
                 ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(new ArrayList<>(users));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("تعذّر حفظ ملف البيانات", e);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
