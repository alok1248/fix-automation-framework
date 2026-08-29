package org.example.fixfw.engine;

import quickfix.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ExecutionReportBus {

    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

    // FIX 4.2 & 4.4 BOTH supported
    public void publish(Message er) {
        queue.offer(er);
    }

    // 🔥 Collect MULTIPLE ERs
    public List<Message> drain(int timeoutSeconds) throws InterruptedException {
        List<Message> result = new ArrayList<>();
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < end) {
            Message m = queue.poll(500, TimeUnit.MILLISECONDS);
            if (m != null) {
                result.add(m);
            }
        }
        return result;
    }

    public void clear() {
        queue.clear();
    }
}
