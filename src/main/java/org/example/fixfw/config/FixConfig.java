package org.example.fixfw.config;

public final class FixConfig {
    private FixConfig() {}

    public static final String QFJ_CFG_CLASSPATH = "config/quickfix.cfg";

    // logon wait
    public static final int LOGON_TIMEOUT_SECONDS = 20;

    // response wait for execution report
    public static final int ER_TIMEOUT_SECONDS = 20;
}
