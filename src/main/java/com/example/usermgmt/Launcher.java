package com.example.usermgmt;

/**
 * يُستخدم لتشغيل البرنامج من الـ classpath مباشرة (مثل IDE) دون إعداد module-path،
 * لأن تشغيل صنف يرث Application مباشرة يسبب خطأ "JavaFX runtime components are missing".
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
