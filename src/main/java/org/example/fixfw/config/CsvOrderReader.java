package org.example.fixfw.config;

import org.example.fixfw.model.OrderData;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CsvOrderReader {

    public static OrderData readFirst(String path) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        if (lines.size() < 2) {
            throw new IllegalArgumentException("CSV has no data rows");
        }

        String[] v = lines.get(1).trim().split(",");

        OrderData d = new OrderData();

        d.setSymbol(v[0]);                        // TCS
        d.setSide(v[1].charAt(0));               // 1
        d.setQty(Integer.parseInt(v[2]));        // 5
        d.setOrdType(v[3].charAt(0));            // 1
        d.setTif(v[4].charAt(0));                // 0

        d.setPrice(Double.parseDouble(v[5]));    // 44
        d.setCurrency(v[6]);                     // 15
        d.setSecurityId(v[7]);                   // 48
        d.setSecurityIdSource(v[8]);   // 22
        d.setExchange(v[9]);                     // 207
        d.setHandlInst(v[10].charAt(0));         // 21
        d.setAccount(v[11]);                     // 1
        d.setSenderSubId(v[12]);                 // 50
        d.setDeliverToCompId(v[13]);
        d.setEmail(v[14]);// 116

        return d;
    }
}
