package org.example.fixfw.utils;

public class MemoryUtil {

    public static long usedMB() {
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
    }
}
