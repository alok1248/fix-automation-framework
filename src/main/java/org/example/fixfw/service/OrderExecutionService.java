package org.example.fixfw.service;

import org.example.fixfw.config.FixConfig;
import org.example.fixfw.engine.FixEngine;
import org.example.fixfw.messaging.MessageSender;
import org.example.fixfw.messaging.NewOrderBuilder44;
import org.example.fixfw.model.OrderData;
import quickfix.Message;

import java.util.List;

public class OrderExecutionService {

    private final FixEngine engine;
    private final MessageSender sender;
    private final NewOrderBuilder44 builder;

    public OrderExecutionService(FixEngine engine) {
        this.engine = engine;
        this.sender = new MessageSender(engine);
        this.builder = new NewOrderBuilder44();
    }

    public List<Message> sendNewOrder(OrderData data, String clOrdId) throws Exception {

        engine.getExecutionReportBus().clear();

        sender.send(builder.build(data, clOrdId));

        return engine.getExecutionReportBus()
                .drain(FixConfig.ER_TIMEOUT_SECONDS);
    }
}
