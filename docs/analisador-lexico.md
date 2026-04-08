# Jack Compiler — Analisador Léxico (Nand2Tetris)

**Estado:** fase léxica **concluída** (scanner + saída `*T.xml` + testes unitários iniciais). **Próxima fase do compilador:** analisador **sintático** (parser, saída `*.xml` completa), com o mesmo ciclo: testes → corrigir bugs → refinar.

## Descrição

Este documento descreve a primeira etapa do projeto: implementar um **analisador léxico (scanner)** para a linguagem **Jack**, conforme o curso Nand2Tetris.

O scanner é responsável por:

- Ler o código fonte `.jack`
- Identificar os tokens da linguagem
- Ignorar espaços em branco e comentários
- Produzir uma saída estruturada (XML) para validação

---

## Objetivos

- Implementar um scanner funcional em Java
- Seguir as regras léxicas da linguagem Jack
- Gerar saída compatível com os arquivos oficiais (`*T.xml`)
- Aplicar **TDD simplificado** (ciclo descrito na secção seguinte)

---

## Desenvolvimento guiado por testes (TDD simplificado)

A partir de agora, o fluxo de trabalho segue:

1. **Escrever um teste** para uma funcionalidade pequena.
2. **Implementar o código** mínimo para passar no teste.
3. **Refatorar** se necessário (ex.: corrigir bugs de espaços em branco).
4. **Avançar** para o próximo token.

Isso se chama **Desenvolvimento Guiado por Testes (TDD)** simplificado.

---

## Estrutura do projeto

```
jack-compiler/
├── src/main/java/jackcompiler/
│   ├── Main.java
│   ├── Scanner.java
│   ├── Token.java
│   └── TokenType.java
├── src/test/java/jackcompiler/   (testes JUnit, quando presentes)
├── nand2tetris/projects/10/    (fixtures oficiais: .jack e *T.xml)
├── docs/
└── pom.xml
```

---

## Requisitos

- Java 17+
- Maven (para `mvn compile` / `mvn test`)
- Git

---

## Como executar

```bash
# Com Maven (raiz do projeto)
mvn compile
mvn test

# Tokenizer → XML (exemplo; ajustar caminho do .jack)
javac -d out -encoding UTF-8 src/main/java/jackcompiler/*.java
java -cp out jackcompiler.Main nand2tetris/projects/10/Square/Main.jack
```

---

## Arquivos de teste (Nand2Tetris)

**Download:** [nand2tetris.org/software](https://www.nand2tetris.org/software)

**Pasta de referência:** `projects/10/`

**Arquivos `.jack` de exemplo:** `Main.jack`, `Square.jack`, `SquareGame.jack`

**Comparar saída de tokens com:** `MainT.xml`, `SquareT.xml`, etc.

---

## Especificação léxica

### Categorias de tokens

| Tipo               | Descrição            |
|--------------------|----------------------|
| `keyword`          | Palavras reservadas  |
| `symbol`           | Símbolos             |
| `integerConstant`  | Números              |
| `stringConstant`   | Strings              |
| `identifier`       | Identificadores      |

### Keywords

`class` `constructor` `function` `method` `field` `static` `var`  
`int` `char` `boolean` `void`  
`true` `false` `null` `this`  
`let` `do` `if` `else` `while` `return`

### Symbols

`{` `}` `(` `)` `[` `]` `.` `,` `;` `+` `-` `*` `/` `&` `|` `<` `>` `=` `~`

### Regras

| Categoria         | Regra |
|-------------------|--------|
| `integerConstant` | Sequência de dígitos |
| `stringConstant`  | Texto entre aspas duplas `" "` |
| `identifier`      | `[a-zA-Z_][a-zA-Z0-9_]*` |

---

## Arquitetura

### Token

Representa a menor unidade da linguagem:

- `type`: `TokenType`
- `lexeme`: `String`
- `line`: `int`

### Scanner

Responsável por:

- Percorrer o código fonte
- Identificar tokens
- Controlar posição e linha
- Ignorar espaços e comentários

---

## Roadmap de implementação

### Fase 1 — Base

- Estrutura do projeto
- Classe `Token`
- Enum `TokenType`
- Scanner básico
- Reconhecimento de: números, identificadores, keywords, símbolos  
  **Status: concluído**

### Fase 2 — Validação

- Geração de XML (`*T.xml`)
- Escape de caracteres: `<`, `>`, `&`, `"`
- Comparação com arquivos oficiais  
  **Status: concluído**

### Fase 3 — Intermediário

- Strings (`"texto"`)
- Comentários: linha (`//`) e bloco (`/* */`)
- Tratamento de erros léxicos  
  **Status: concluído**

### Fase 4 — Qualidade

- Testes automatizados (JUnit), fluxo: escrever/rodar testes → falhas → corrigir bugs → repetir
- Refatoração do código
- Melhorias de performance (se necessário)  
  **Status: testes unitários iniciais concluídos** (base para manter regressões sob controlo)

---

### Próxima fase (fora deste documento): analisador sintático

- Parser recursivo (gramática Jack) e árvore / saída `*.xml` (não só tokens).
- Mesmo método de trabalho: **rodar testes** (oficiais do projeto 10 e/ou JUnit), **comparar com gabaritos**, **corrigir bugs**, iterar até bater com os `.xml` de referência.
- Documentação específica pode ser acrescentada mais tarde (ex.: `docs/analisador-sintatico.md`).

---

## Regras do projeto

- Código original (sem copiar soluções prontas)
- Histórico de commits progressivo
- Organização clara do código

---

## Observações

- O XML de tokens (`*T.xml`) serve para **validação** do scanner.
- O scanner é a **base** do parser sintático (o parser consome a sequência de tokens).
- O compilador completo Jack → VM segue, após o sintático, para geração de VM (outros projetos Nand2Tetris).
