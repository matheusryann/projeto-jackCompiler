package symbol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SymbolTableTest {

    private SymbolTable table;

    @BeforeEach
    void setUp() {
        table = new SymbolTable();
    }

    @Test
    void defineStaticAndField_assignsIndices() {
        table.define("count", "int", SymbolTable.KIND_STATIC);
        table.define("width", "int", SymbolTable.KIND_FIELD);
        table.define("height", "int", SymbolTable.KIND_FIELD);

        assertEquals(0, table.indexOf("count"));
        assertEquals(SymbolTable.KIND_STATIC, table.kindOf("count"));
        assertEquals("int", table.typeOf("count"));

        assertEquals(0, table.indexOf("width"));
        assertEquals(1, table.indexOf("height"));
        assertEquals(SymbolTable.KIND_FIELD, table.kindOf("height"));

        assertEquals(1, table.varCount(SymbolTable.KIND_STATIC));
        assertEquals(2, table.varCount(SymbolTable.KIND_FIELD));
    }

    @Test
    void startSubroutine_clearsArgAndVar_keepsClassScope() {
        table.define("x", "int", SymbolTable.KIND_STATIC);
        table.startSubroutine();
        table.define("a", "int", SymbolTable.KIND_ARG);
        table.define("i", "int", SymbolTable.KIND_VAR);

        assertEquals(0, table.indexOf("a"));
        assertEquals(0, table.indexOf("i"));
        assertEquals(0, table.indexOf("x"));

        table.startSubroutine();
        assertFalse(table.contains("a"));
        assertFalse(table.contains("i"));
        assertTrue(table.contains("x"));
        assertEquals(0, table.indexOf("x"));
    }

    @Test
    void defineArgAndVar_inSubroutineScope() {
        table.startSubroutine();
        table.define("x", "int", SymbolTable.KIND_ARG);
        table.define("y", "int", SymbolTable.KIND_ARG);
        table.define("sum", "int", SymbolTable.KIND_VAR);

        assertEquals(0, table.indexOf("x"));
        assertEquals(1, table.indexOf("y"));
        assertEquals(2, table.varCount(SymbolTable.KIND_ARG));
        assertEquals(0, table.indexOf("sum"));
        assertEquals(1, table.varCount(SymbolTable.KIND_VAR));
    }

    @Test
    void lookup_prefersSubroutineOverClass() {
        table.define("a", "int", SymbolTable.KIND_STATIC);
        table.startSubroutine();
        table.define("a", "boolean", SymbolTable.KIND_VAR);

        assertEquals(SymbolTable.KIND_VAR, table.kindOf("a"));
        assertEquals("boolean", table.typeOf("a"));
        assertEquals(0, table.indexOf("a"));
    }

    @Test
    void indexOf_unknownName_throws() {
        assertThrows(IllegalStateException.class, () -> table.indexOf("missing"));
    }

    @Test
    void reset_clearsEverything() {
        table.define("s", "int", SymbolTable.KIND_STATIC);
        table.startSubroutine();
        table.define("v", "int", SymbolTable.KIND_VAR);

        table.reset();

        assertFalse(table.contains("s"));
        assertFalse(table.contains("v"));
        assertEquals(0, table.varCount(SymbolTable.KIND_STATIC));
        assertEquals(0, table.varCount(SymbolTable.KIND_VAR));
    }
}
