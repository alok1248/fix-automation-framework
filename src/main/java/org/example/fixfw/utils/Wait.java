package org.example.fixfw.utils;

public final class Wait {
    private Wait() {}
    public static void sleepMs(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
