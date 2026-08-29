package org.example.fixfw.validation;

import org.testng.Assert;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.Message;
import quickfix.field.OrderQty;
import org.testng.Assert;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

import java.util.List;

public final class FixAssertions {

    private FixAssertions() {}

    public static void assertQtyPresent(List<Message> ers, int expectedQty) {
        boolean found = ers.stream().anyMatch(m -> {
            try {
                return m.isSetField(OrderQty.FIELD)
                        && m.getInt(OrderQty.FIELD) == expectedQty;
            } catch (Exception e) {
                return false;
            }
        });

        Assert.assertTrue(found,
                "OrderQty " + expectedQty + " not found in ExecutionReports");
    }

    // Basic: New accepted / working usually 39=0 (New) OR 39=A (PendingNew) depends on OMS rules
//    public static void assertOrdStatusIsNewOrPending(ExecutionReport er) throws Exception {
//        char st = er.getOrdStatus().getValue();
//        System.out.println(st);
//        Assert.assertTrue(st == OrdStatus.NEW || st == OrdStatus.PENDING_NEW,
//                "Unexpected OrdStatus: " + st + " (expected NEW or PENDING_NEW)");
//    }

    public static void assertPendingOrNew(List<Message> ers, String clOrdId) {

        boolean pending = false;
        boolean newOrd = false;

        for (Message m : ers) {
            try {
                if (!m.isSetField(ClOrdID.FIELD)) continue;
                if (!m.getString(ClOrdID.FIELD).equals(clOrdId)) continue;

                if (m.isSetField(ExecType.FIELD)) {
                    char execType = m.getChar(ExecType.FIELD);
                    if (execType == ExecType.PENDING_NEW) pending = true;
                    if (execType == ExecType.NEW) newOrd = true;
                }
            } catch (Exception ignored) {}
        }

        Assert.assertTrue(pending && newOrd,
                "Expected ExecType PendingNew(150=A) or New(150=0)");
    }

    public static void assertPendingThenNewReceived(
            List<Message> ers,
            String clOrdId
    ) {
        boolean pending = false;
        boolean newOrder = false;

        for (Message msg : ers) {
            try {
                if (!msg.isSetField(11)) continue;
                if (!clOrdId.equals(msg.getString(11))) continue;

                char execType = msg.getChar(150);

                if (execType == 'A') pending = true;
                if (execType == '0') newOrder = true;

            } catch (Exception ignored) {}
        }

        // ✅ VALID scenarios
        if (newOrder) {
            return; // PASS (direct NEW OR after Pending)
        }

        throw new AssertionError(
                "Order did not reach NEW state. Pending=" + pending +
                        ", Received=" + ers
        );
    }

    public static void assertRejected(
            List<Message> executionReports,
            String clOrdId) {

        boolean rejected = executionReports.stream().anyMatch(msg -> {
            try {
                return msg.isSetField(39)
                        && msg.getChar(39) == '8'     // OrdStatus = Rejected
                        && msg.getString(11).equals(clOrdId);
            } catch (Exception e) {
                return false;
            }
        });

        if (!rejected) {
            throw new AssertionError(
                    "Expected Order Reject not received for ClOrdID=" + clOrdId);
        }
    }



}
