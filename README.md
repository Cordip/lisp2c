# lisp2c

Lisp-to-C compiler on Scala 3.

## Prerequisites

- JDK 17+
- scala-cli
- GCC
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
just help                 # examples
just run file.lisp        # compile and run from file
just run-expr "(+ 1 2)"   # compile and run expression
just run-expr 42          # bare expression
just run-all              # run all examples
just test                 # run tests
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

## Как работают замыкания (на примере make-adder)

Рассмотрим пример:

```lisp
(define (make-adder n) (lambda (x) (+ n x)))
```

**Шаг 1 — FreeVarAnalysis** обнаруживает, что `n` является свободной переменной во внутренней лямбде: она не входит в её список параметров (`x`), но используется в её теле.

**Шаг 2 — Lowering** превращает каждую лямбду в `CFunction` (C-функцию) и порождает `CClosure` — описание того, что нужно захватить. Внешняя функция `make-adder` получает тело в виде `CClosure("lambda_1", List(CParam(0)))`, то есть «создать замыкание для `lambda_1`, захватив первый параметр — `n`». Внутренняя функция `lambda_1` обращается к `n` через `CEnvRef(0)` (`env->vars[0]`), а к `x` — через `CParam(0)` (`argv[0]`).

**Шаг 3 — Flatten** раскрывает `CClosure` в конкретные операторы:

```c
Env* env_0 = make_env(1);      // выделить среду на 1 переменную
env_0->vars[0] = argv[0];      // записать n (он же argv[0] внешней функции)
LispVal v0 = make_closure(lambda_1, env_0);  // упаковать функцию + среду
return v0;
```

Имя `env_0` (а не просто `env`) формируется счётчиком свежих имён внутри Flatten, чтобы избежать коллизий при нескольких замыканиях в одном файле.

**Шаг 4 — Вызов замыкания.** `apply_closure` проверяет тег, извлекает из `ClosureData` указатель на функцию и среду, затем вызывает:

```c
data->fn(data->env, argc, argv);
```

То есть среда передаётся первым аргументом, а за ней — количество аргументов и их массив.

**Структура `ClosureData`** содержит два поля: указатель на C-функцию и указатель на `Env` с захваченными значениями:

```c
struct ClosureData {
    LispVal (*fn)(Env *env, int argc, LispVal *argv);
    Env *env;
};
```

**Сигнатура всех пользовательских функций** единообразна — параметр `env` принимает захваченный контекст (для функций без замыкания туда передаётся пустая среда `make_env(0)`):

```c
LispVal fn(Env *env, int argc, LispVal *argv);
```
