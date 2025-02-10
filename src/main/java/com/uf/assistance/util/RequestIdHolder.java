package com.uf.assistance.util;

public class RequestIdHolder {
    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    public static void set(String id) {
        requestId.set(id);
    }

    public static String get() {
        return requestId.get();
    }

    public static void clear() {
        requestId.remove();
    }
}
