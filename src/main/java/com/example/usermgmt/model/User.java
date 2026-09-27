package com.example.usermgmt.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** بيانات حساب مستخدم مسجّل. */
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String email;
    private final int age;
    private final String passwordHash;
    private final LocalDateTime createdAt;
    private AccountStatus status;

    public User(long id, String firstName, String lastName, String phone, String email,
                int age, String passwordHash) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.age = age;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
        this.status = AccountStatus.PENDING;
    }

    public long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public AccountStatus getStatus() { return status; }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
