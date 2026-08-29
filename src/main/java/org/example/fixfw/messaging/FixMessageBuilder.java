package org.example.fixfw.messaging;

import org.example.fixfw.model.OrderData;
import quickfix.Message;

public interface FixMessageBuilder<T extends Message> {
    T build(OrderData data, String clOrdId);
}
