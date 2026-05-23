package vm;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Emite instruções da VM Jack (nand2tetris) — uma por linha.
 */
public class VMWriter implements Closeable {

    public static final String SEG_CONST = "constant";
    public static final String SEG_ARG = "argument";
    public static final String SEG_LOCAL = "local";
    public static final String SEG_STATIC = "static";
    public static final String SEG_THIS = "this";
    public static final String SEG_THAT = "that";
    public static final String SEG_POINTER = "pointer";
    public static final String SEG_TEMP = "temp";

    private final PrintWriter out;

    public VMWriter(PrintWriter out) {
        this.out = out;
    }

    public void writePush(String segment, int index) {
        out.println("push " + segment + " " + index);
    }

    public void writePop(String segment, int index) {
        out.println("pop " + segment + " " + index);
    }

    /**
     * @param command add, sub, neg, eq, gt, lt, and, or, not
     */
    public void writeArithmetic(String command) {
        out.println(command);
    }

    public void writeLabel(String label) {
        out.println("label " + label);
    }

    public void writeGoto(String label) {
        out.println("goto " + label);
    }

    public void writeIf(String label) {
        out.println("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        out.println("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nLocals) {
        out.println("function " + name + " " + nLocals);
    }

    public void writeReturn() {
        out.println("return");
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
