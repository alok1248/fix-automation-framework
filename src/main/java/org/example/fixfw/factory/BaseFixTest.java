package org.example.fixfw.factory;

import io.qameta.allure.Attachment;
import org.example.fixfw.config.FixConfig;
import org.example.fixfw.engine.FixEngine;
import org.example.fixfw.engine.FixEngineImpl;
//import org.example.fixfw.repo.OrderContext;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import quickfix.SessionID;

public abstract class BaseFixTest {

    protected static FixEngine engine;
    //protected static OrderContext ctx;

    private static final SessionID FIX42_SOSUV =
            new SessionID("FIX.4.2", "BANZAI", "SOSUV_SERVER"); // ✅ must match quickfix.cfg

    @BeforeSuite
    public void beforeSuite() throws Exception {
        engine = new FixEngineImpl();
        //ctx = new OrderContext();

        engine.start();

        boolean loggedOn = engine.getApp().awaitLogon(FIX42_SOSUV, FixConfig.LOGON_TIMEOUT_SECONDS);
        Assert.assertTrue(loggedOn, "FIX42 Logon not received in " + FixConfig.LOGON_TIMEOUT_SECONDS + " seconds");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (engine != null) engine.stop();
    }

    @Attachment(value = "FIX Message", type = "text/plain")
    public static String attachFix(String msg) {
        return msg;
    }
}
