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

## Scala сущности и их назначение

Ниже — кратко про каждую Scala-сущность в `src/main/scala` и её роль в компиляторном конвейере.

### `Main.scala`
- `@main def lisp2c(args: String*)` — CLI-вход: читает выражение/файл, запускает `Compiler.pipeline`, записывает результат через `Compiler.initializeOutput`.

### `lisp/orchestration/Compiler.scala`
- `object Compiler` — оркестратор всего пайплайна.
  - `pipeline` — полный путь `Lisp source -> C file text` (Tokenizer, Parser, Transform, FreeVarAnalysis, Lowering, Flatten, CodeGen, Printer + template.c).
  - `initializeOutput` — создаёт `output/` и пишет `output.c`, `runtime.c/.h`, `tags.h`.
  - `writeFile`, `readResource` — служебные I/O-функции.

### `lisp/parse/Tokenizer.scala`
- `object Tokenizer`
  - `apply` — лексер: разбивает исходную строку на токены (`(`, `)`, `'`, символы, числа, булевы литералы).

### `lisp/parse/Parser.scala`
- `object Parser`
  - `apply` — парсит одно S-выражение.
  - `parseAll` — парсит поток токенов в список S-выражений верхнего уровня.
  - `parse`, `parseList` — внутренняя рекурсивная логика разбора списков, quote-формы и атомов.

### `lisp/transform/Transform.scala`
- `object Transform`
  - `apply` / `transform` — переводит `SExpr` в более семантический `LispExpr`.
  - Распознаёт special forms: `if`, `quote`, `define`, `lambda`; всё остальное — `LispApply`.
  - `paramNames`, `transformQuoted` — обработка параметров и quoted-структур.

### `lisp/transform/FreeVarAnalysis.scala`
- `object FreeVarAnalysis`
  - `apply` / `analyze` — вычисляет свободные переменные `LispLambda` (для замыканий).
  - `collectFree` — рекурсивный сбор свободных переменных с учётом scope и встроенных примитивов.

### `lisp/transform/Lowering.scala`
- `object Lowering` — lowering из `LispExpr` в `CExpr`.
  - `apply`, `lowerProgram` — lowering одного выражения или всей программы (включая список функций и глобалов).
  - `lowerTopLevel`, `lowerExprWithName`, `lowerExpr`, `lowerLambda`, `lowerQuote` — ключевые этапы трансляции.
  - `primitiveMap` — соответствие Lisp-примитивов runtime-функциям C.
- `Scope` (private case class) — контекст разрешения имён (параметры, env, глобалы, let-переменные).
- `LoweringState` (private class) — аккумулятор с уникальными именами, списком сгенерированных функций и глобалов.

### `lisp/emit/Runtime.scala`
- `object Runtime` — единый справочник имён C-runtime функций/констант и множества Lisp-примитивов.

### `lisp/emit/Flatten.scala`
- `object Flatten` — уплощает вложенные `CExpr` в линейные `Statement`.
  - `apply`, `flattenBody`, `flattenTopLevelAll` — flatten для функции и top-level выражений.
  - `flattenOneTopLevel`, `flattenToVar`, `flatten` — внутренняя логика нормализации выражений в шаги с временными переменными.
- `FlattenState` (private class) — генератор уникальных имён (`vN`, `env_N`).
- `FlattenCtx` (private class) — контекст накопления `Statement` и создание вложенных веток (`if`).

### `lisp/emit/CodeGen.scala`
- `object CodeGen`
  - `apply` — перевод `Statement` в структурные строки `Line`.
  - `renderGlobalDecl` — генерация объявления глобала.
  - `renderFunction` — генерация C-функции из `FlatFunction`.
  - `emitStatement`, `emitExpr` — внутренний рендеринг инструкций и выражений в C-синтаксис.

### `lisp/emit/Printer.scala`
- `object Printer`
  - `apply` — собирает `Line` в финальный текст C-кода.
  - `render` — рекурсивная печать с отступами (поддержка `Text` и `Block`).

### `lisp/types/SExpr.scala`
- `enum SExpr` — синтаксический уровень после парсинга:
  - `SList`, `SNumber`, `SBool`, `SSymbol`, `SNil`.

### `lisp/types/LispExpr.scala`
- `enum LispExpr` — семантический AST Lisp:
  - `LispCons`, `LispNumber`, `LispBool`, `LispSymbol`, `LispQuote`, `LispIf`, `LispApply`, `LispVar`, `LispNil`, `LispLambda`, `LispDefine`, `LispLet`.
  - `show` — человекочитаемое представление узла.

### `lisp/types/CExpr.scala`
- `enum CExpr` — промежуточное представление выражений, близкое к C:
  - `CCall`, `CIf`, `CNumber`, `CStringLit`, `CVar`, `CClosure`, `CParam`, `CApplyClosure`, `CDefineAssign`, `CLet`, `CEnvRef`, `CArgArray`.

### `lisp/types/Statement.scala`
- `enum Statement` — плоские инструкции для C-генерации:
  - `Return`, `Value`, `Assign`, `If`, `Define`, `EnvDecl`, `EnvSet`, `PrintVal`.

### `lisp/types/Line.scala`
- `enum Line` — дерево строк для форматтера:
  - `Text` (обычная строка), `Block` (вложенный блок с дополнительным отступом).

### `lisp/types/CFunction.scala`
- `case class CFunction(name, params, body)` — функция в CExpr-представлении (до flatten).
- `case class FlatFunction(name, params, body)` — функция после flatten (`body: List[Statement]`).
- `case class GlobalDecl(name)` — описание глобального объявления.
