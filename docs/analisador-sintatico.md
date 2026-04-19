# Jack Compiler — Analisador Sintático (Nand2Tetris)

**Estado:** planeamento e especificação (implementação a seguir).

**Pré-requisito:** analisador léxico funcional — lista de `Token` compatível com os gabaritos `*T.xml` (ver [analisador-lexico.md](analisador-lexico.md)).

---

## Contrato da fase sintática

| Aspeto | Especificação |
|--------|----------------|
| **Entrada** | Lista de tokens produzida pelo **Scanner** (incluindo `EOF` como marcador de fim, conforme a vossa convenção interna). |
| **Saída** | Ficheiro **XML** que representa a **árvore sintática (parse tree)** de um programa Jack — o mesmo formato dos ficheiros `*.xml` (sem sufixo `T`) do Project 10. |
| **Validação** | Comparação byte-a-byte ou linha-a-linha com os **ficheiros oficiais** em `nand2tetris/projects/10/` (ex.: `Main.xml`, `Square.xml`, `SquareGame.xml`, e variantes `ExpressionLessSquare`, `ArrayTest`). |
| **Metodologia** | **Desenvolvimento incremental** com **TDD**: pequenos incrementos de gramática + testes (JUnit e/ou testes de regressão contra XML oficiais) antes de avançar. |

---

## Diferença entre `*T.xml` e `*.xml`

- **`*T.xml` (tokenizer):** apenas `<tokens>` … `</tokens>` com elementos `<keyword>`, `<symbol>`, `<integerConstant>`, `<stringConstant>`, `<identifier>`.
- **`*.xml` (parser):** árvore aninhada com etiquetas de regra sintática, por exemplo `<class>`, `<classVarDec>`, `<subroutineDec>`, `<statements>`, `<expression>`, `<term>`, etc., cada uma contendo tokens e sub-árvores no mesmo estilo de espaçamento que os gabaritos Nand2Tetris.

O analisador sintático **não substitui** o tokenizer: **consome** a sequência de tokens e **emite** o XML estruturado.

---

## Arquitetura sugerida (visão geral)

1. **Scanner** — lê `.jack` → `List<Token>`.
2. **Parser** — recebe `List<Token>` (ou um iterador com *lookahead*), implementa a gramática Jack (tipicamente **descendente recursiva**), e escreve XML para `String`/ficheiro.
3. **Validação** — para cada `.jack` de teste, gerar `Nome.generated.xml` e comparar com `Nome.xml` oficial.

Fluxo conceptual:

```
.jack  →  Scanner.tokenize()  →  [Token, …, EOF]
                                      ↓
                            Parser.parse() / compileClass()
                                      ↓
                         Main.xml (parse tree em XML)
```

---

## Formato de saída XML (parse tree)

A estrutura segue o **Jack Grammar** e o **XML output** descritos no *Project 10* do curso: cada nó não-terminal é uma etiqueta XML; folhas são sempre os mesmos tipos de token que no `*T.xml`, mas **aninhados** sob as regras corretas.

Exemplos de etiquetas de não-terminais (lista não exaustiva; ver gabaritos):

- `class`, `classVarDec`, `type`, `subroutineDec`, `parameterList`, `subroutineBody`
- `varDec`, `statements`, `letStatement`, `ifStatement`, `whileStatement`, `doStatement`, `returnStatement`
- `expression`, `term`, `expressionList`, `op`, `unaryOp`, `keywordConstant`

**Espaçamento:** os ficheiros oficiais usam um padrão fixo (ex.: ` <keyword> class </keyword> ` com espaços à volta do lexema). Qualquer diferença de espaço ou quebra de linha falha comparação literal — convém **copiar o estilo dos gabaritos** ou normalizar só em testes.

---

## Validação com os gabaritos Nand2Tetris

**Pasta de referência:** `nand2tetris/projects/10/`

Conjuntos úteis:

| Pasta | Uso típico |
|-------|------------|
| `Square/` | Caso completo com expressões e chamadas |
| `ExpressionLessSquare/` | Gramática mínima (útil para primeiro incremento do parser) |
| `ArrayTest/` | Arrays e indexação |

**Processo:**

1. Compilar e executar o compilador sobre cada `*.jack`.
2. Escrever `*.generated.xml` (ou nome acordado no grupo).
3. Comparar com o `*.xml` oficial correspondente (`fc` no Windows, `diff` no Unix, ou `Compare-Object` no PowerShell).
4. Corrigir o parser até coincidir (ou até normalizar diferenças só de whitespace, se deliberadamente aceites).

---

## Desenvolvimento incremental + TDD

Ordem sugerida (alinhada ao curso e aos exemplos `ExpressionLess` vs completos):

1. **Infraestrutura:** leitura sequencial de tokens, `match`/consumo de token esperado, relatório de erro sintático claro (token obtido vs esperado).
2. **Gramática “expression-less”:** `class`, variáveis de classe, declaração de subrotinas, corpo mínimo, `let`/`do`/`return` sem expressões completas — validar contra `ExpressionLessSquare/*.xml`.
3. **Expressões:** `expression`, `term`, operadores, chamadas, `expressionList` — integrar com `Square/`.
4. **Statements completos:** `if`, `while` com expressões.
5. **Arrays:** `ArrayTest/`.

Em cada passo:

- Escrever teste(s) que fixem o comportamento (JUnit com ficheiros pequenos **ou** teste de integração que compare XML gerado vs oficial).
- Implementar o mínimo para verde.
- Refatorar e repetir.

---

## Erros sintáticos

Para a fase de **parse tree** do Project 10, o foco é **árvore correta** para programas válidos. Para programas inválidos, pode-se definir uma política simples: mensagem com linha/token e abortar; testes oficiais assumem normalmente **Jack válido**.

---

## Próxima fase (após o sintático)

- **Geração de código VM** (Project 11): a árvore sintática (ou uma AST interna) alimenta o gerador de instruções VM — fora do âmbito deste documento até o parser estar estável.

---

## Referências

- *Nand2Tetris*, Project 10 — Compiler I: Syntax Analysis (especificação da gramática e do XML).
- Software e materiais: [nand2tetris.org](https://www.nand2tetris.org/software).
