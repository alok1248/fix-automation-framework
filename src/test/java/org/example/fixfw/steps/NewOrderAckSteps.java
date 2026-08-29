package org.example.fixfw.steps;

import io.cucumber.java.en.*;
import org.example.fixfw.config.CsvOrderReader;
import org.example.fixfw.context.FixTestContext;
import org.example.fixfw.engine.FixApp;
import org.example.fixfw.factory.BaseFixTest;
import org.example.fixfw.model.OrderData;
import org.example.fixfw.service.OrderExecutionService;
import org.example.fixfw.utils.FixAttachmentUtil;
import org.example.fixfw.validation.FixAssertions;
import quickfix.Message;
import org.testng.Assert;

import java.util.List;

public class NewOrderAckSteps extends BaseFixTest{

    private final FixTestContext context;
    private final OrderExecutionService orderService;
    public FixApp fixApp;

    public NewOrderAckSteps() {
        this.context = Hooks.getContext();
        this.orderService =
                new OrderExecutionService(context.getEngine());
    }

//    @Given("FIX session is up")
//    public void fix_session_is_up() throws Exception{
//        Thread.sleep(5000);
//        //fixApp.waitForLogon(10000);
//        attachFix("FIX session is established");
//    }

//    @When("client sends NewOrderSingle with qty {int}")
//    public void client_sends_new_order(int expectedQty) throws Exception {
//
//        OrderData data =
//                CsvOrderReader.readFirst(
//                        "src/main/resources/strategies/order.csv"
//                );
//
//        data.setQty(expectedQty);
//        String clOrdId = "ORD-" + System.currentTimeMillis();
//        context.setClOrdId(clOrdId);
//
//        List<Message> ers =
//                orderService.sendNewOrder(data, clOrdId);
//
//        context.setExecutionReports(ers);
//    }

    @Then("server should send ExecutionReport Ack with qty {int}")
    public void server_should_send_ack_with_qty(int qty) {

        List<Message> ers = context.getExecutionReports();

        FixAttachmentUtil.attachAll("ER RECEIVED", ers);
        FixAssertions.assertQtyPresent(ers, qty);

        FixAssertions.assertPendingOrNew(
                context.getExecutionReports(),
                context.getClOrdId()
        );
    }
}
