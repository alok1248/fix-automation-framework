package org.example.fixfw.validation;

import org.example.fixfw.utils.MemoryUtil;
import org.testng.Assert;

public class MemoryValidator {

    public static long snapshot() {
        return MemoryUtil.usedMB();
    }

    public static void assertNoLeak(long before, long limitMb) {
        long after = MemoryUtil.usedMB();
        long delta = after - before;

        Assert.assertTrue(delta < limitMb,
                "Memory leak suspected. Used=" + delta + "MB");
    }
}
