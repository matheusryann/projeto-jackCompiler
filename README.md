# Compilador Jack — Analisador Léxico e Sintático

**Disciplina:** Compiladores  
**Curso:** Engenharia da Computação — Universidade Federal do Maranhão

## Integrantes

- Matheus Ryan Carreiro Costa Correia
- João Gabriel de Oliveira Lopes

## Descrição

Este projeto implementa, em **Java**, as etapas iniciais de um compilador para a linguagem **Jack**, utilizada no curso **Nand2Tetris**. A implementação atual contempla a **análise léxica** e a **análise sintática**, com geração de arquivos XML compatíveis com o formato esperado no Project 10 do Nand2Tetris.

O desenvolvimento foi organizado de forma incremental, separando a responsabilidade de cada etapa do processo de compilação.

## Fluxo de execução

O fluxo geral do projeto é:

```text
Arquivo .jack
    ↓
Analisador léxico (Scanner)
    ↓
Lista de tokens
    ↓
Analisador sintático (Parser)
    ↓
XML da árvore sintática
```

A análise sintática depende da análise léxica, pois o parser não processa diretamente o texto do arquivo `.jack`; ele recebe a sequência de tokens produzida pelo scanner.

## Funcionalidades implementadas

- Leitura de arquivos fonte `.jack`.
- Identificação de tokens da linguagem Jack:
  - palavras-chave;
  - símbolos;
  - identificadores;
  - constantes inteiras;
  - constantes string.
- Geração de XML de tokens.
- Construção da árvore sintática em XML para estruturas da linguagem Jack.
- Testes automatizados com JUnit.

## Organização do código

```text
src/
├── Main.java
├── ParserMain.java
├── io/
│   └── TokensXmlWriter.java
├── lexer/
│   ├── Scanner.java
│   ├── Token.java
│   └── TokenType.java
└── parser/
    └── Parser.java

 tests/parser/
 └── ParserTest.java
```

### Principais componentes

| Componente | Responsabilidade |
|-----------|------------------|
| `Scanner` | Realiza a análise léxica do código Jack. |
| `Token` e `TokenType` | Representam os tokens reconhecidos. |
| `TokensXmlWriter` | Escreve o XML gerado pelo analisador léxico. |
| `Parser` | Realiza a análise sintática a partir da lista de tokens. |
| `Main` | Executa a geração do XML de tokens. |
| `ParserMain` | Executa o fluxo léxico + sintático e gera o XML da árvore sintática. |

## Requisitos

- Java 17 ou superior
- Maven 3.x

## Compilação

Na raiz do projeto, execute:

```bash
mvn compile
```

## Execução do analisador léxico

A classe `Main` executa apenas a análise léxica e gera um arquivo no formato `NomeT.generated.xml`.

Exemplo:

```bash
mvn -q compile exec:java "-Dexec.mainClass=Main" "-Dexec.args=nand2tetris/projects/10/Square/Main.jack
```

Também é possível executar diretamente com Java:

```bash
java -cp target/classes Main nand2tetris/projects/10/Square/Main.jack
```

## Execução do analisador sintático

A classe `ParserMain` executa o scanner e, em seguida, o parser, gerando um arquivo no formato `NomeP.generated.xml`.

Exemplo:

```bash
mvn -q compile exec:java -Dexec.mainClass=ParserMain -Dexec.args="nand2tetris/projects/10/Square/Main.jack"
```

Também é possível executar diretamente com Java:

```bash
java -cp target/classes ParserMain nand2tetris/projects/10/Square/Main.jack
```

## Testes

Os testes automatizados podem ser executados com:

```bash
mvn clean test
```

Os testes verificam principalmente o comportamento do parser em diferentes estruturas da linguagem Jack, como classes, expressões, termos, comandos e chamadas de subrotina.

## Saídas geradas

| Etapa | Classe executada | Arquivo gerado |
|------|------------------|----------------|
| Análise léxica | `Main` | `NomeT.generated.xml` |
| Análise sintática | `ParserMain` | `NomeP.generated.xml` |

Os arquivos gerados podem ser comparados com os XMLs de referência fornecidos pelo Nand2Tetris.

## Estado atual do projeto

Atualmente, o projeto implementa as etapas de análise léxica e análise sintática. A próxima etapa natural seria a geração de código intermediário para a VM, correspondente à continuidade do compilador Jack proposta pelo Nand2Tetris.
