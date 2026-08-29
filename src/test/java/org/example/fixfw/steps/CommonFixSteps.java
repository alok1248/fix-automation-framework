package org.example.fixfw.steps;

import io.cucumber.java.en.Given;
import org.example.fixfw.config.CsvOrderReader;
import org.example.fixfw.factory.BaseFixTest;
import org.example.fixfw.model.OrderData;
import io.cucumber.java.en.*;
import org.example.fixfw.context.FixTestContext;
import org.example.fixfw.engine.FixApp;
import org.example.fixfw.service.OrderExecutionService;
import quickfix.Message;

import java.util.List;

public class CommonFixSteps extends BaseFixTest {

    private final FixTestContext context;
    private final OrderExecutionService orderService;
    public FixApp fixApp;

    public CommonFixSteps(){
        this.context = Hooks.getContext();
        this.orderService =
                new OrderExecutionService(context.getEngine());
    }

    @Given("FIX session is up")
    public void fix_session_is_up() throws Exception {
        Thread.sleep(3000);
        attachFix("FIX session is established");
    }

    @When("client sends NewOrderSingle with qty {int}")
    public void client_sends_new_order(int expectedQty) throws Exception {

        OrderData data =
                CsvOrderReader.readFirst(
                        "src/main/resources/strategies/order.csv"
                );

        data.setQty(expectedQty);

        String clOrdId = "ORD-" + System.currentTimeMillis();
        context.setClOrdId(clOrdId);

        List<Message> ers =
                orderService.sendNewOrder(data, clOrdId);

        context.setExecutionReports(ers);
    }
}
