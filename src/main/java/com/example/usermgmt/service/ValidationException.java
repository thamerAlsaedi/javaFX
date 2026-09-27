package com.example.usermgmt.service;

/** خطأ في التحقق من صحة البيانات المُدخلة، رسالته معروضة للمستخدم. */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
