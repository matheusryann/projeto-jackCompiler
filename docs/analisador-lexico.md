# Jack Compiler — Analisador Léxico (Nand2Tetris)

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
- Aplicar desenvolvimento incremental (TDD)

---

## Estrutura do projeto

```
jack-compiler/
├── src/main/java/jackcompiler/
│   ├── Main.java
│   ├── Scanner.java
│   ├── Token.java
│   └── TokenType.java
├── tests/
├── nand2tetris/
│   └── projects/10/
└── README.md
```

---

## Requisitos

- Java 17+ (recomendado)
- Maven (opcional)
- Git

---

## Como executar

```bash
# Compilar
javac src/main/java/jackcompiler/*.java

# Executar
java jackcompiler.Main
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

### Fase 2 — Intermediário

- Strings (`"texto"`)
- Comentários: linha (`//`) e bloco (`/* */`)
- Tratamento de erros léxicos

### Fase 3 — Validação

- Geração de XML
- Escape de caracteres: `<`, `>`, `&`, `"`
- Comparação com arquivos oficiais

### Fase 4 — Qualidade

- Testes automatizados (JUnit)
- Refatoração do código
- Melhorias de performance

---

## Estratégia de testes

Abordagem incremental:

1. Implementar uma funcionalidade pequena
2. Testar
3. Ajustar
4. Avançar

---

## Regras do projeto

- Código original (sem copiar soluções prontas)
- Histórico de commits progressivo
- Organização clara do código

---

## Observações

- O XML gerado é principalmente para **validação** do scanner
- O scanner será a base do compilador completo
- Este trabalho integra a construção de um compilador Jack → VM
