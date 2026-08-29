package org.example.fixfw.engine;

import quickfix.SessionID;

public interface FixEngine {
    void start() throws Exception;
    void stop();

    FixApp getApp();
    ExecutionReportBus getExecutionReportBus();

    SessionID getSessionId();  // active logged-on session
}
