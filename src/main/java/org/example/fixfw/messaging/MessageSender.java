package org.example.fixfw.messaging;

import org.example.fixfw.engine.FixEngine;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;

public class MessageSender {

    private final FixEngine engine;

    public MessageSender(FixEngine engine) {
        this.engine = engine;
    }

    public void send(Message msg) throws SessionNotFound {
        SessionID sid = engine.getSessionId();
        if (sid == null) throw new IllegalStateException("No active session (logon not completed).");

        Session s = Session.lookupSession(sid);
        if (s == null) throw new IllegalStateException("Session not found in QFJ: " + sid);
        if (!s.isLoggedOn()) throw new IllegalStateException("Session not logged on: " + sid);

        Session.sendToTarget(msg, sid);
        System.out.println("[FIX] SENT => " + msg + " to " + sid);
    }
}
