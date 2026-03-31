# lisp2c

Lisp-to-C compiler on Scala 3.

## Prerequisites

- JDK 17+
- sbt
- GCC
- [Boehm GC](https://github.com/ivmai/bdwgc)
- [just](https://github.com/casey/just)

## Pipeline

```
(+ 1 2)                                 — Lisp source
  ↓ Tokenizer
["(", "+", "1", "2", ")"]               — tokens
  ↓ Parser
SList(SSymbol("+"), SNumber(1), ...)     — S-expression
  ↓ Transform
LispApply(LispSymbol("+"), ...)          — AST
  ↓ Lowering
CCall("lisp_add", ...)                  — C expression tree
  ↓ Flatten
[Value("v0", ...), Return(CVar("v2"))]  — flat statements
  ↓ CodeGen
[Text("LispVal v0 = ..."), ...]         — Line tree
  ↓ Printer
"  LispVal v0 = make_int(1);\n..."      — indented C code
  ↓ GCC
./output/program                        — binary
```

## Usage

```bash
just help              # show all commands
just run file.lisp     # compile and run from file
just run -e "(+ 1 2)"  # compile and run expression
just run -e 42         # bare expression
just run-all           # run all examples
just test              # run tests
```

Output is written to `output/` directory and compiled with GCC automatically.

## Project structure

```
src/main/scala/
  Main.scala                    — entry point
  lisp/
    types/
      SExpr.scala               — S-expression types
      LispExpr.scala            — Lisp AST types
      CExpr.scala               — C expression types
      Statement.scala           — flat C statement types
      Line.scala                — output line tree (Text/Block)
    parse/
      Tokenizer.scala           — string → tokens
      Parser.scala              — tokens → SExpr
    transform/
      Transform.scala           — SExpr → LispExpr
      Lowering.scala            — LispExpr → CExpr
    emit/
      Flatten.scala             — CExpr tree → flat Statements
      CodeGen.scala             — Statements → List[Line]
      Printer.scala             — Line tree → indented string
      Runtime.scala             — runtime function names
    orchestration/
      Compiler.scala            — pipeline and file output

src/main/resources/
  runtime.h                     — C runtime header
  runtime.c                     — C runtime implementation
  template.c                    — C output template

scripts/                        — build scripts for justfile
```
