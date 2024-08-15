package com.mndk.bteterrarenderer.printer;

import java.util.Arrays;
import java.util.List;

public class TableColumnPrinter implements Printer {

    private final List<String> lines;

    TableColumnPrinter(List<String> lines) {
        this.lines = lines;
    }

    public void print(String content) {
        List<String> split = Arrays.asList(content.split("\n"));
        int lineCount = split.size();
        for(int i = 0; i < lineCount; ++i) {
            lines.add(content);
        }
    }
}
