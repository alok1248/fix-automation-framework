package org.example.fixfw.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import org.example.fixfw.context.FixTestContext;
import org.example.fixfw.engine.FixEngine;
import org.example.fixfw.engine.FixEngineImpl;

public class Hooks {

    private static FixEngine engine;
    private static FixTestContext context;

    @BeforeAll
    public static void startFix() throws Exception {
        engine = new FixEngineImpl();
        engine.start();

        context = new FixTestContext();
        context.setEngine(engine);
    }

    @AfterAll
    public static void stopFix() {
        if (engine != null) {
            engine.stop();
        }
    }

    public static FixTestContext getContext() {
        return context;
    }
}
