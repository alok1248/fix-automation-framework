package org.example.fixfw.context;

import org.example.fixfw.engine.FixEngine;
import quickfix.Message;

import java.util.List;

public class FixTestContext {

    private FixEngine engine;
    private List<Message> executionReports;
    private String clOrdId;

    public FixEngine getEngine() {
        return engine;
    }

    public void setEngine(FixEngine engine) {
        this.engine = engine;
    }

    public List<Message> getExecutionReports() {
        return executionReports;
    }

    public void setExecutionReports(List<Message> executionReports) {
        this.executionReports = executionReports;
    }

    public String getClOrdId() {
        return clOrdId;
    }

    public void setClOrdId(String clOrdId) {
        this.clOrdId = clOrdId;
    }
}
