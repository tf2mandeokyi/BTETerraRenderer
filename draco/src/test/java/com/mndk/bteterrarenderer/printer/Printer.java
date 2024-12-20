package com.mndk.bteterrarenderer.printer;

import java.io.PrintStream;

@FunctionalInterface
@SuppressWarnings("unused")
public interface Printer {
    static Printer of(PrintStream stream) { return stream::print; }
    static Printer stdout() { return of(System.out); }

    void print(String content);
}
