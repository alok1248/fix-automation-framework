package org.example.fixfw.messaging;

import org.example.fixfw.model.OrderData;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;

import java.time.LocalDateTime;
import java.util.Date;

public class NewOrderBuilder44 implements FixMessageBuilder<NewOrderSingle> {

    @Override
    public NewOrderSingle build(OrderData d, String clOrdId) {

        // FIX4.2 constructor requires: ClOrdID, HandlInst, Symbol, Side, TransactTime, OrdType
        NewOrderSingle nos = new NewOrderSingle(
                new ClOrdID(clOrdId),
                new HandlInst(d.getHandlInst()),
                new Symbol(d.getSymbol()),
                new Side(d.getSide()),
                new TransactTime(),
                new OrdType(d.getOrdType())
        );

        nos.set(new OrderQty(d.getQty()));
        nos.set(new TimeInForce(d.getTif()));
        nos.set(new Price(d.getPrice()));
        nos.set(new Currency(d.getCurrency()));
        nos.set(new SecurityID(d.getSecurityId()));
        nos.set(new SecurityExchange(d.getExchange()));
        nos.set(new Account(d.getAccount()));


        // Header fields
        nos.getHeader().setField(new SenderSubID(d.getSenderSubId()));
        nos.getHeader().setField(new DeliverToCompID(d.getDeliverToCompId()));
        nos.getHeader().setField(new SecurityIDSource(d.getSecurityIdSource()));
        nos.getHeader().setField( new OnBehalfOfSubID(d.getEmail()));

        return nos;
    }
}
