package symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * Tabelas de símbolos de classe e de subrotina (nand2tetris Project 11).
 * <p>
 * Escopo de classe: {@link #KIND_STATIC}, {@link #KIND_FIELD}.
 * Escopo de subrotina: {@link #KIND_ARG}, {@link #KIND_VAR}.
 */
public class SymbolTable {

    public static final int KIND_STATIC = 0;
    public static final int KIND_FIELD = 1;
    public static final int KIND_ARG = 2;
    public static final int KIND_VAR = 3;

    private static final class Symbol {
        final String type;
        final int kind;
        final int index;

        Symbol(String type, int kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    private final Map<String, Symbol> classSymbols = new HashMap<>();
    private final Map<String, Symbol> subroutineSymbols = new HashMap<>();
    private final int[] classCounts = new int[2];
    private final int[] subroutineCounts = new int[2];

    /** Nova classe: limpa escopos de classe e de subrotina. */
    public void reset() {
        classSymbols.clear();
        subroutineSymbols.clear();
        classCounts[0] = 0;
        classCounts[1] = 0;
        subroutineCounts[0] = 0;
        subroutineCounts[1] = 0;
    }

    /** Nova subrotina: limpa apenas variáveis e argumentos locais à subrotina. */
    public void startSubroutine() {
        subroutineSymbols.clear();
        subroutineCounts[0] = 0;
        subroutineCounts[1] = 0;
    }

    /**
     * Métodos Jack: {@code argument 0} é o receiver ({@code this}); parâmetros declarados começam em 1.
     */
    public void reserveArgumentZeroForThis() {
        subroutineCounts[0] = 1;
    }

    public void define(String name, String type, int kind) {
        if (kind == KIND_STATIC || kind == KIND_FIELD) {
            int index = classCounts[kind]++;
            classSymbols.put(name, new Symbol(type, kind, index));
            return;
        }
        if (kind == KIND_ARG || kind == KIND_VAR) {
            int slot = kind - KIND_ARG;
            int index = subroutineCounts[slot]++;
            subroutineSymbols.put(name, new Symbol(type, kind, index));
            return;
        }
        throw new IllegalArgumentException("Kind inválido: " + kind);
    }

    /** Quantidade de variáveis já definidas daquele kind no escopo correspondente. */
    public int varCount(int kind) {
        if (kind == KIND_STATIC || kind == KIND_FIELD) {
            return classCounts[kind];
        }
        if (kind == KIND_ARG || kind == KIND_VAR) {
            return subroutineCounts[kind - KIND_ARG];
        }
        throw new IllegalArgumentException("Kind inválido: " + kind);
    }

    /**
     * Busca primeiro na subrotina, depois na classe (padrão nand2tetris).
     */
    public int indexOf(String name) {
        return lookup(name).index;
    }

    public String typeOf(String name) {
        return lookup(name).type;
    }

    public int kindOf(String name) {
        return lookup(name).kind;
    }

    public boolean contains(String name) {
        return subroutineSymbols.containsKey(name) || classSymbols.containsKey(name);
    }

    private Symbol lookup(String name) {
        Symbol symbol = subroutineSymbols.get(name);
        if (symbol == null) {
            symbol = classSymbols.get(name);
        }
        if (symbol == null) {
            throw new IllegalStateException("Identificador não definido: " + name);
        }
        return symbol;
    }
}
