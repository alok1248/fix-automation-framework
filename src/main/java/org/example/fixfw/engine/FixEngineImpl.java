package org.example.fixfw.engine;

import org.example.fixfw.config.FixConfig;
import org.example.fixfw.engine.ExecutionReportBus;
import org.example.fixfw.engine.FixApp;
import quickfix.*;

import java.io.InputStream;

public class FixEngineImpl implements FixEngine {

    private SocketInitiator initiator;
    private FixApp app;
    private ExecutionReportBus erBus;

    @Override
    public void start() throws Exception {
        InputStream cfgStream = getClass().getClassLoader().getResourceAsStream(FixConfig.QFJ_CFG_CLASSPATH);
        if (cfgStream == null) throw new IllegalStateException("quickfix.cfg not found in resources");

        SessionSettings settings = new SessionSettings(cfgStream);

        erBus = new ExecutionReportBus();
        app = new FixApp(erBus);

        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(app, storeFactory, settings, logFactory, messageFactory);
        initiator.start();

        System.out.println("[FIX] Initiator started");
    }

    @Override public void stop() { if (initiator != null) initiator.stop(); }
    @Override public FixApp getApp() { return app; }
    @Override public ExecutionReportBus getExecutionReportBus() { return erBus; }
    @Override public SessionID getSessionId() { return app.getActiveSessionId(); }
}
