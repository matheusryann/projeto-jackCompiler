package vm;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VMWriterTest {

    private static String emit(WriterAction action) {
        StringWriter buffer = new StringWriter();
        try (VMWriter writer = new VMWriter(new PrintWriter(buffer))) {
            action.write(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return buffer.toString().replace("\r\n", "\n");
    }

    @FunctionalInterface
    private interface WriterAction {
        void write(VMWriter writer) throws Exception;
    }

    @Test
    void writePush_constant() {
        String out = emit(w -> w.writePush(VMWriter.SEG_CONST, 5));
        assertEquals("push constant 5\n", out);
    }

    @Test
    void writePop_local() {
        String out = emit(w -> w.writePop(VMWriter.SEG_LOCAL, 2));
        assertEquals("pop local 2\n", out);
    }

    @Test
    void writeArithmetic_add() {
        String out = emit(w -> w.writeArithmetic("add"));
        assertEquals("add\n", out);
    }

    @Test
    void writeLabel_goto_ifGoto() {
        String out = emit(w -> {
            w.writeLabel("L0");
            w.writeGoto("L1");
            w.writeIf("L2");
        });
        assertEquals("""
                label L0
                goto L1
                if-goto L2
                """, out);
    }

    @Test
    void writeCall() {
        String out = emit(w -> w.writeCall("Math.multiply", 2));
        assertEquals("call Math.multiply 2\n", out);
    }

    @Test
    void writeFunction() {
        String out = emit(w -> w.writeFunction("Main.main", 0));
        assertEquals("function Main.main 0\n", out);
    }

    @Test
    void writeReturn() {
        String out = emit(w -> w.writeReturn());
        assertEquals("return\n", out);
    }

    @Test
    void sequence_pushAddPop() {
        String out = emit(w -> {
            w.writePush(VMWriter.SEG_CONST, 1);
            w.writePush(VMWriter.SEG_CONST, 2);
            w.writeArithmetic("add");
            w.writePop(VMWriter.SEG_LOCAL, 0);
        });
        assertEquals("""
                push constant 1
                push constant 2
                add
                pop local 0
                """, out);
    }
}
