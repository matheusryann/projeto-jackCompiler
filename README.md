# Compilador Jack — trabalho de Compiladores

**Disciplina:** Compiladores (ou equivalente)  
**Curso:** Engenharia da Computação - Universidade Federal do Maranhão

Este repositório contém o **compilador da linguagem Jack** do curso [Nand2Tetris](https://www.nand2tetris.org/), implementado em **Java**. O objetivo didático é percorrer as fases típicas de um compilador: **análise léxica** → **análise sintática** (árvore em XML) → **geração de código** para a VM (etapas futuras), alinhado aos *Projects 10 e 11* do material oficial.

## Integrantes

- Matheus Ryan Carreiro Costa Correia  
- João Gabriel de Oliveira Lopes  

---

## O que o programa faz (visão rápida)

Hoje, ao executar a aplicação principal (`Main`), o projeto **tokeniza** um arquivo `.jack` e grava um XML de tokens (`*T.generated.xml`) compatível com o formato do Nand2Tetris. O **parser** já existe em parte (testes cobrem expressões e uma classe mínima); a árvore sintática completa e o backend da VM vêm nas próximas entregas.

---

## Requisitos de ambiente

| Ferramenta | Versão |
|------------|--------|
| Java | 17 ou superior |
| Maven | 3.x |

---

## Como executar

**1.** Abra um terminal na pasta do projeto (onde está o arquivo `pom.xml`).

**2.** Compilar:

```bash
mvn compile
```

**3.** Rodar o **tokenizer** (gera o XML de tokens ao lado do `.jack`). Depois de `mvn compile`, podes usar **uma** destas formas.

**Com Maven (invoca a `Main` com o classpath certo):** no **PowerShell**, o `-Dexec.args=...` tem de ir **inteiro entre aspas duplas**, senão o comando pode ser cortado ao meio:

```powershell
mvn -q compile exec:java "-Dexec.args=nand2tetris/projects/10/Square/Main.jack"
```

No **Git Bash** ou **Linux/macOS**:

```bash
mvn -q compile exec:java -Dexec.args="nand2tetris/projects/10/Square/Main.jack"
```

**Só com `java` (sem plugin exec):** a partir da pasta do projeto, depois de `mvn compile`:

```text
java -cp target/classes jackcompiler.Main nand2tetris/projects/10/Square/Main.jack
```

Troque o caminho pelo seu `.jack` (relativo ou absoluto).

**4.** Rodar os **testes automatizados** (JUnit):

```bash
mvn test
```

---

## Entrada esperada

| Quem consome | O que deve receber |
|--------------|-------------------|
| **Linha de comando (`java … jackcompiler.Main`)** | **Exatamente um** argumento: caminho para um arquivo fonte **`.jack`** (texto, UTF-8). Outras extensões são rejeitadas. |
| **Parser nos testes** | Lista de `Token` vinda do `Scanner`, em geral incluindo `EOF` no fim. |

---

## Saída gerada

| Etapa | Onde aparece | Formato |
|-------|----------------|---------|
| **Tokenizer (o que a `Main` gera hoje)** | No **mesmo diretório** do `.jack`, arquivo `NomeT.generated.xml` | XML com raiz `<tokens>`, uma tag por token (`keyword`, `symbol`, `integerConstant`, `stringConstant`, `identifier`), como nos gabaritos `*T.xml` do Nand2Tetris. |
| **Parser (em construção)** | Por enquanto, principalmente **saída em memória** nos testes | XML da árvore sintática (sem `T` no nome), estilo Project 10 — ainda não cobre um programa Jack inteiro. |

Em caso de sucesso, o programa **imprime na tela** o caminho absoluto do `*T.generated.xml` criado.

---

## O que já está implementado (por pacote)

- **`jackcompiler.lexer`** — analisador léxico (`Scanner`), representação de tokens (`Token`, `TokenType`), comentários de linha e bloco, serialização dos tokens para XML (`Token#toXml()`).
- **`jackcompiler.parser`** — parser descendente recursivo: fragmentos da gramática (expressão, termo, lista de expressões, chamadas) e `class` vazia; coberto por `ParserTest`.
- **`jackcompiler.io`** — escrita do XML do tokenizer em disco (`TokensXmlWriter`).
- **`jackcompiler.Main`** — ponto de entrada: lê o `.jack`, chama o lexer e grava o `*T.generated.xml`.

Estrutura de pastas:

```
src/main/java/jackcompiler/
├── Main.java
├── io/TokensXmlWriter.java
├── lexer/Scanner.java, Token.java, TokenType.java
└── parser/Parser.java

src/test/java/jackcompiler/parser/ParserTest.java
```

---
## Próximas etapas (disciplina)

- Completar o parser para todo o programa Jack e comparar com os `.xml` oficiais.  
- Geração de código VM (Project 11).  
