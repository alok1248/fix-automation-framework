package org.example.fixfw.utils;

import org.example.fixfw.factory.BaseFixTest;
import quickfix.Message;

import java.util.List;

public class FixAttachmentUtil {

    public static void attachAll(String prefix, List<Message> messages) {
        for (Message msg : messages) {
            BaseFixTest.attachFix(prefix + ":\n" + msg.toString());
        }
    }
    public static void attachText(String prefix, String text) {
        BaseFixTest.attachFix(prefix + ":\n" + text);
    }

}
