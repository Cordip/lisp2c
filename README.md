# lisp2c

Lisp-to-C compiler on Scala 3.

## Pipeline

```
(42)                                    — Lisp source
  ↓ Tokenizer
["(", "42", ")"]                        — tokens
  ↓ Parser
SList(SNumber(42))                      — S-expression
  ↓ Transform
LispCons(LispNumber(42), LispNil)       — AST
  ↓ Lowering
CCall("make_cons", ...)                 — C expression tree
  ↓ Flatten
[Value("v0", ...), Return(CVar("v0"))]  — flat statements
  ↓ CodeGen
"LispVal* v0 = make_int(42);\n..."      — C code
  ↓ GCC
./output/program                        — binary
```

## Usage

```bash
just help            # show all commands
just build "(42)"    # Lisp → C → GCC (compile only)
just run "(42)"      # compile and run
just run file.lisp   # compile and run from file
just run-all         # run all examples
just test            # run tests
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
      CStatement.scala          — flat C statement types
    parse/
      Tokenizer.scala           — string → tokens
      Parser.scala              — tokens → SExpr
    transform/
      Transform.scala           — SExpr → LispExpr
      Lowering.scala            — LispExpr → CExpr
    emit/
      Flatten.scala             — CExpr tree → flat CStatements
      CodeGen.scala             — CStatements → C code string
      Runtime.scala             — runtime function names
    orchestration/
      Compiler.scala            — pipeline and file output

src/main/resources/
  runtime.h                     — C runtime header
  runtime.c                     — C runtime implementation
  template.c                    — C output template

scripts/                        — build scripts for justfile
```
