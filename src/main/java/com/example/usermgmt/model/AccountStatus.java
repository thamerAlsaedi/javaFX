package com.example.usermgmt.model;

/** حالات حساب المستخدم. */
public enum AccountStatus {
    PENDING("بانتظار التفعيل"),
    ACTIVE("مفعّل"),
    DISABLED("معطّل"),
    BANNED("محظور");

    private final String arabicLabel;

    AccountStatus(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String getArabicLabel() {
        return arabicLabel;
    }

    @Override
    public String toString() {
        return arabicLabel;
    }
}
