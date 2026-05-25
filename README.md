# Compilador Jack - Gerador de Codigo VM

**Disciplina:** Compiladores  
**Curso:** Engenharia da Computacao - Universidade Federal do Maranhao

## Integrantes

- Matheus Ryan Carreiro Costa Correia
- Joao Gabriel de Oliveira Lopes

## Descricao

Este projeto implementa, em Java, um compilador para a linguagem Jack do curso
nand2tetris. A etapa atual gera codigo intermediario da maquina virtual (`.vm`)
a partir dos arquivos fonte `.jack`, dando continuidade as etapas anteriores de
analise lexica e analise sintatica.

O compilador aceita como entrada um arquivo `.jack` individual ou um diretorio
contendo arquivos `.jack`. Ao receber um diretorio, todos os arquivos Jack
encontrados sao compilados e cada classe gera um arquivo `.vm` correspondente no
mesmo diretorio do arquivo de origem.

## Fluxo de compilacao

```text
Arquivo ou diretorio .jack
    -> coleta dos arquivos Jack
    -> analise lexica
    -> compilation engine
    -> tabela de simbolos
    -> escrita de comandos VM
    -> arquivos .vm
```

## Funcionalidades implementadas

- Leitura de arquivo `.jack` unico.
- Leitura de diretorios com varios arquivos `.jack`.
- Geracao de um arquivo `.vm` para cada classe Jack.
- Geracao de codigo VM para:
  - expressoes aritmeticas, logicas e relacionais;
  - comandos `let`, `if`, `while`, `do` e `return`;
  - variaveis `static`, `field`, `argument` e `local`;
  - chamadas de funcoes, metodos e construtores;
  - strings;
  - arrays;
  - objetos e referencias via `this`.
- Integracao com tabela de simbolos para resolver escopo, tipo, indice e
  segmento VM de cada identificador.
- Rotulos unicos para comandos condicionais e lacos.
- Testes automatizados com JUnit.

## Organizacao do codigo

```text
src/
|-- Compiler.java
|-- Main.java
|-- ParserMain.java
|-- io/
|   |-- JackFileCollector.java
|   `-- TokensXmlWriter.java
|-- lexer/
|   |-- Scanner.java
|   |-- Token.java
|   `-- TokenType.java
|-- parser/
|   |-- CompilationEngine.java
|   `-- Parser.java
|-- symbol/
|   `-- SymbolTable.java
`-- vm/
    `-- VMWriter.java

tests/
|-- io/
|-- parser/
|-- symbol/
`-- vm/
```

## Principais componentes

| Componente | Responsabilidade |
|-----------|------------------|
| `Compiler` | Ponto de entrada da etapa VM. Recebe arquivo ou diretorio e gera `.vm`. |
| `JackFileCollector` | Coleta arquivos `.jack` a partir de arquivo unico ou diretorio. |
| `Scanner` | Converte o codigo Jack em tokens. |
| `CompilationEngine` | Percorre a estrutura sintatica e emite comandos VM. |
| `SymbolTable` | Controla escopos de classe e subrotina. |
| `VMWriter` | Escreve comandos VM no formato esperado pelo VM Emulator. |
| `Parser` | Parser XML usado na etapa anterior do projeto. |
| `Main` e `ParserMain` | Entradas auxiliares das etapas anteriores de lexer/parser. |

## Requisitos

- Java 17 ou superior
- Maven 3.x
- Ferramentas oficiais nand2tetris para validacao no VM Emulator

## Como reproduzir a compilacao em lote e os testes

Primeiro, compile o projeto Java:

```bash
mvn compile
```

Depois, execute o compilador apontando para cada diretorio oficial do Project
11. O compilador ira localizar os arquivos `.jack` do diretorio e gerar os
arquivos `.vm` correspondentes:

```bash
java -cp target/classes Compiler nand2tetris/projects/11/Seven
java -cp target/classes Compiler nand2tetris/projects/11/Average
java -cp target/classes Compiler nand2tetris/projects/11/ConvertToBin
java -cp target/classes Compiler nand2tetris/projects/11/ComplexArrays
java -cp target/classes Compiler nand2tetris/projects/11/Square
java -cp target/classes Compiler nand2tetris/projects/11/Pong
```

Para conferir a geracao dos arquivos, verifique os `.vm` criados dentro de cada
pasta. Exemplos:

```text
nand2tetris/projects/11/Seven/Main.vm
nand2tetris/projects/11/Square/Main.vm
nand2tetris/projects/11/Square/Square.vm
nand2tetris/projects/11/Square/SquareGame.vm
nand2tetris/projects/11/Pong/Ball.vm
nand2tetris/projects/11/Pong/Bat.vm
nand2tetris/projects/11/Pong/Main.vm
nand2tetris/projects/11/Pong/PongGame.vm
```

Em seguida, abra o VM Emulator oficial do nand2tetris, carregue a pasta do
programa testado, selecione `Animate: No animation` e execute com o botao de
run. Para projetos com multiplos arquivos, como `Square` e `Pong`, a pasta
inteira deve ser carregada no emulator.

Os testes unitarios tambem podem ser reproduzidos com:

```bash
mvn test
```

Os testes cobrem componentes como:

- escrita de comandos VM pelo `VMWriter`;
- controle de escopo pela `SymbolTable`;
- coleta de arquivos Jack por arquivo ou diretorio;
- comportamentos principais do parser.

## Validacao no VM Emulator

A validacao funcional foi feita com os programas oficiais do Project 11:

| Programa | Resultado validado |
|----------|--------------------|
| `Seven` | Imprime `7`. |
| `Average` | Entrada `3, 10, 20, 30`; imprime media `20`. |
| `ConvertToBin` | Entrada em `RAM[8000] = 7`; bits gerados em `RAM[8001...]`. |
| `ComplexArrays` | Todos os testes exibem `expected result` igual ao `actual result`. |
| `Square` | Projeto com multiplos arquivos compila e executa o quadrado interativo. |
| `Pong` | Projeto com multiplos arquivos compila e executa o jogo Pong. |

Durante os testes completos no VM Emulator, a animacao foi desativada
(`Animate: No animation`) para acelerar a execucao.

## Desafios enfrentados

Um dos principais desafios foi integrar a geracao de codigo VM ao analisador
lexico e ao parser ja existentes, mantendo o fluxo de compilacao organizado. A
compilation engine passou a precisar nao apenas reconhecer a estrutura do codigo
Jack, mas tambem emitir os comandos VM corretos enquanto controla escopos,
variaveis locais, argumentos, campos e variaveis estaticas.

Outro ponto importante foi tratar corretamente chamadas de subrotinas, metodos e
construtores. Nesses casos, foi necessario diferenciar chamadas feitas pela
classe, por objetos e pelo proprio objeto atual (`this`), alem de ajustar a
quantidade de argumentos enviada para a VM.

Tambem houve cuidado especial com arrays e strings. Arrays exigem manipulacao
correta dos segmentos `pointer` e `that`, enquanto strings precisam ser
construidas por chamadas para `String.new` e `String.appendChar`. Esses casos
foram importantes para que programas mais completos, como `Square` e `Pong`,
funcionassem corretamente.

Por fim, a validacao no VM Emulator exigiu testes incrementais. Programas
simples, como `Seven`, ajudaram a validar expressoes e retornos. Em seguida,
programas como `Average`, `ComplexArrays`, `Square` e `Pong` permitiram testar
controle de fluxo, arrays, objetos, multiplos arquivos e compilacao por
diretorio.

## Etapas anteriores

As entradas das etapas anteriores continuam disponiveis.

Geracao de XML de tokens:

```bash
java -cp target/classes Main nand2tetris/projects/10/Square/Main.jack
```

Geracao de XML sintatico:

```bash
java -cp target/classes ParserMain nand2tetris/projects/10/Square/SquareGame.jack
```
