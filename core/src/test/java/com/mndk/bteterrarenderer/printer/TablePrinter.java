package com.mndk.bteterrarenderer.printer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TablePrinter {

    private static final String ATTACH_DELIMITER = " ";

    // Outer list = column, inner list = row
    private final List<List<String>> table = new ArrayList<>();

    public void print(PrintStream stream) {
        int[] widths = new int[table.size()];
        int maxRowCount = 0;
        for(int columnIndex = 0; columnIndex < table.size(); ++columnIndex) {
            List<String> column = table.get(columnIndex);
            widths[columnIndex] = -1;
            for(String row : column) {
                widths[columnIndex] = Math.max(widths[columnIndex], row.length());
            }
            maxRowCount = Math.max(maxRowCount, column.size());
        }

        for(int lineIndex = 0; lineIndex < maxRowCount; ++lineIndex) {
            for(int columnIndex = 0; columnIndex < table.size(); ++columnIndex) {
                List<String> column = table.get(columnIndex);
                String cell = lineIndex < column.size() ? column.get(lineIndex) : "";
                if(widths[columnIndex] != -1) {
                    stream.printf("%-" + widths[columnIndex] + "s", cell);
                    stream.print(ATTACH_DELIMITER);
                }
            }
            stream.println();
        }
        stream.flush();
        table.clear();
    }

    public TableColumnPrinter newColumn() {
        List<String> column = new ArrayList<>();
        table.add(column);
        return new TableColumnPrinter(column);
    }

}
