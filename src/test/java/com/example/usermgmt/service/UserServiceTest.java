package com.example.usermgmt.service;

import com.example.usermgmt.model.AccountStatus;
import com.example.usermgmt.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @TempDir
    Path dir;

    private UserService newService() {
        return new UserService(dir.resolve("users.dat"));
    }

    @Test
    void registerCreatesPendingUser() throws Exception {
        User user = newService().register("أحمد", "علي", "0512345678", "A@Mail.com", "25", "secret1");
        assertEquals(AccountStatus.PENDING, user.getStatus());
        assertEquals("a@mail.com", user.getEmail());
    }

    @Test
    void rejectsInvalidInput() {
        UserService s = newService();
        assertThrows(ValidationException.class, () -> s.register("", "علي", "0512345678", "a@mail.com", "25", "secret1"));
        assertThrows(ValidationException.class, () -> s.register("أحمد", "علي", "123", "a@mail.com", "25", "secret1"));
        assertThrows(ValidationException.class, () -> s.register("أحمد", "علي", "0512345678", "bad", "25", "secret1"));
        assertThrows(ValidationException.class, () -> s.register("أحمد", "علي", "0512345678", "a@mail.com", "abc", "secret1"));
        assertThrows(ValidationException.class, () -> s.register("أحمد", "علي", "0512345678", "a@mail.com", "5", "secret1"));
    }

    @Test
    void rejectsDuplicateEmailAndPhone() throws Exception {
        UserService s = newService();
        s.register("أحمد", "علي", "0512345678", "a@mail.com", "25", "secret1");
        assertThrows(ValidationException.class, () -> s.register("سعد", "محمد", "0599999999", "a@mail.com", "30", "secret1"));
        assertThrows(ValidationException.class, () -> s.register("سعد", "محمد", "0512345678", "b@mail.com", "30", "secret1"));
    }

    @Test
    void statusChangesPersistAcrossRestarts() throws Exception {
        UserService s = newService();
        User user = s.register("أحمد", "علي", "0512345678", "a@mail.com", "25", "secret1");
        s.changeStatus(user, AccountStatus.BANNED);

        UserService reloaded = newService();
        assertEquals(AccountStatus.BANNED, reloaded.getAllUsers().get(0).getStatus());
        assertTrue(reloaded.authenticate("a@mail.com", "secret1").isPresent());
        assertTrue(reloaded.authenticate("a@mail.com", "wrong").isEmpty());
    }
}
