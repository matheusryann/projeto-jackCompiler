# Instruções para agentes — Compilador Jack (Project 11)

**Leia primeiro:** [`docs/project11-gerador-vm-agente.md`](docs/project11-gerador-vm-agente.md)

Esse documento contém o estado do repositório, arquitetura alvo, API de `SymbolTable`/`VMWriter`/`CompilationEngine`, compilação em lote, mapeamento Jack→VM, ordem de testes (Seven → Pong) e definição de pronto.

## Contexto em uma linha

Fases léxico (`Scanner`) e sintático (`Parser` → XML) **concluídas**; falta **geração VM** + classe **`Compiler`** com suporte a **diretórios**.

## Comandos úteis

```bash
mvn clean test
mvn -q compile
java -cp target/classes ParserMain nand2tetris/projects/10/Square/Main.jack
```

## Regras para o agente

1. Trabalhar em `project-jackCompiler-fresh/`; não copiar código de soluções públicas.
2. Testar incrementalmente: Seven antes de Pong.
3. Compilar **pastas inteiras** (`Square/`, `Pong/`) é requisito obrigatório.
4. Corrigir inconsistência de pacote `parser` vs `jackcompiler.parser` no início da fase VM.
5. Commits só quando o usuário pedir.
