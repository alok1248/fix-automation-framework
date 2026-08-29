package org.example.fixfw.validation;

import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;
import quickfix.field.ClOrdID;

public class ExecutionReportMatcher {

    public boolean matchesClOrdId(ExecutionReport er, String clOrdId) {
        try {
            if (!er.isSetField(ClOrdID.FIELD)) return false;
            return clOrdId.equals(er.getString(ClOrdID.FIELD));
        } catch (FieldNotFound e) {
            return false;
        }
    }
}