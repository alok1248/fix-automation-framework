package org.example.fixfw.engine;

import quickfix.*;
import quickfix.fix44.ExecutionReport;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FixApp extends MessageCracker implements Application {

    private volatile boolean loggedOn = false;

    private final Set<SessionID> loggedOnSessions = ConcurrentHashMap.newKeySet();
    private final ExecutionReportBus erBus;

    private volatile SessionID activeSessionId;

    public FixApp(ExecutionReportBus erBus) {
        this.erBus = erBus;
    }

    public boolean awaitLogon(SessionID expected, int timeoutSeconds) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < end) {
            if (loggedOnSessions.contains(expected)) return true;
            Thread.sleep(200);
        }
        return false;
    }

    public void waitForLogon(long timeoutMs) {

        long start = System.currentTimeMillis();

        while (!loggedOn) {
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw new IllegalStateException(
                        "FIX logon not completed within " + timeoutMs + " ms"
                );
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public SessionID getActiveSessionId() {
        return activeSessionId;
    }

    @Override
    public void onCreate(SessionID sessionID) {
        System.out.println("[FIX] onCreate: " + sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        System.out.println("[FIX] LOGON: " + sessionID);
        loggedOnSessions.add(sessionID);   // ✅ MUST
        this.activeSessionId = sessionID;
    }


    @Override
    public void onLogout(SessionID sessionID) {
        System.out.println("[FIX] LOGOUT: " + sessionID);
        loggedOnSessions.remove(sessionID);
        if (sessionID.equals(activeSessionId)) activeSessionId = null;
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        System.out.println("[FIX] toAdmin: " + sessionID + " => " + message);
        // username/password not needed -> keep empty
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectTagValue, RejectLogon {
        System.out.println("[FIX] fromAdmin: " + sessionID + " <= " + message);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        System.out.println("[FIX] toApp: " + sessionID + " => " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        System.out.println("[FIX] fromApp: " + sessionID + " <= " + message);
        try{
            crack(message, sessionID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // MessageCracker reflection se call hota hai
    public void onMessage(quickfix.fix42.ExecutionReport er, SessionID sessionID)
            throws FieldNotFound {

        System.out.println("[FIX] ER RECEIVED => " + er);
        erBus.publish(er);   // ✅ NOW WORKS
    }
}
