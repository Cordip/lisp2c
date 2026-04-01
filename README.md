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

## Как устроены замыкания (closure) в этом проекте

Ниже — практическое объяснение именно для `lisp2c`, а не абстрактной теории.

### 1) Что такое замыкание

Замыкание — это функция + сохранённое окружение (значения внешних переменных, которые функция использует).

Пример на Lisp:

```lisp
(define make-adder
  (lambda (n)
    (lambda (x) (+ x n))))
```

Внутренний `lambda (x) ...` использует `n`, хотя `n` не является его параметром.
Значит, `n` должен быть захвачен в окружение этой функции.

### 2) Как проект понимает, что надо захватывать

За это отвечает анализ свободных переменных (`FreeVarAnalysis`).

- Для каждого `LispLambda` вычисляется список `freeVars`.
- В `freeVars` попадают переменные, которые:
  - используются в теле лямбды;
  - не являются параметрами этой лямбды;
  - не являются примитивами рантайма (`+`, `-`, `*`, `cons` и т.д.).

То есть для:

```lisp
(lambda (x) (+ x n))
```

будет рассчитано примерно:

```scala
LispLambda(
  params = List("x"),
  body = LispApply(LispVar("+"), List(LispVar("x"), LispVar("n"))),
  freeVars = List("n")
)
```

### 3) Как это превращается в C-структуры

На этапе `Lowering` лямбда превращается в две вещи:

1. **Отдельная C-функция** (`CFunction`) с телом лямбды.
2. **Объект замыкания** (`CClosure`) — ссылка на функцию + массив захваченных значений.

Для примера выше концептуально получается так:

- генерируется функция вроде `lambda_0` с параметром `x`;
- переменная `x` внутри тела читается как обычный параметр;
- переменная `n` читается не как параметр, а через `CEnvRef(0)` (первый элемент окружения);
- при создании closure туда кладётся текущее значение `n`.

Идея в терминах внутренних узлов:

```scala
CFunction("lambda_0", List("x"), /* тело, где n -> CEnvRef(0) */)
CClosure("lambda_0", List(scope.resolve("n")))
```

### 4) Почему это работает правильно

Когда `make-adder` вызывается, создаётся **новое** замыкание для конкретного `n`.
Поэтому:

```lisp
(define add10 (make-adder 10))
(define add20 (make-adder 20))
```

`add10` и `add20` ссылаются на одну и ту же сгенерированную функцию, но с **разными окружениями**.
В одном closure хранится `10`, в другом — `20`.

---

## Как работает Lowering (подробно)

`Lowering` — это перевод `LispExpr` (высокоуровневое AST) в `CExpr` (низкоуровневое дерево, ближе к C runtime).

Если кратко:

- **вход**: `LispExpr`
- **выход**: `CExpr` + список сгенерированных функций + список глобальных объявлений (для программы)

### 1) Контекст видимости (`Scope`)

Во время lowering используется `Scope`, где хранятся:

- `params` — параметры текущей функции;
- `envVars` — захваченные переменные текущего closure;
- `globals` — имена верхнеуровневых `define`;
- `letVars` — локальные переменные `let` (после переименования);
- `parent` — родительский scope (механизм предусмотрен структурой).

При встрече переменной `LispVar(name)` резолвинг идёт по порядку:

1. `letVars` → `CVar(...)`
2. `params` → `CParam(index)`
3. `envVars` → `CEnvRef(index)`
4. `globals` → `CVar(sanitizeName(name))`
5. иначе ошибка `unresolved variable`

### 2) Базовые выражения

- `LispNumber(n)` → `CCall(make_int, [n])`
- `LispNil` → `CVar(lisp_nil)`
- `LispBool(true/false)` → `CVar(lisp_true/lisp_false)`
- `LispCons(a, b)` → `CCall(make_cons, [...])`

Это значит, что даже простые значения приводятся к runtime-представлению `LispVal`.

### 3) Примитивы и обычные вызовы

Есть таблица примитивов (`primitiveMap`), например:

- `+ -> lisp_add`
- `- -> lisp_sub`
- `* -> lisp_mul`
- `car -> lisp_car`
- `cdr -> lisp_cdr`

Если встречается вызов вида:

```lisp
(+ 1 2)
```

то lowering делает прямой runtime-вызов:

```scala
CCall("lisp_add", List(...))
```

Если это не примитив, то вызов идёт как вызов closure:

```scala
CApplyClosure(loweredFn, loweredArgs)
```

То есть пользовательские функции вызываются через механизм closure.

### 4) Лямбды

`LispLambda` lowering-ится в:

- добавление новой `CFunction` в состояние;
- возврат `CClosure` как значения выражения.

Внутри тела этой функции:

- параметры читаются через `CParam`;
- захваченные переменные через `CEnvRef`.

### 5) `define` на верхнем уровне

В `lowerProgram`:

- сначала собираются имена всех top-level `define` в `globals`;
- для каждого `define` добавляется `GlobalDecl`;
- значение `define` lowering-ится и присваивается в `CDefineAssign`.

Это нужно, чтобы глобальные имена были доступны в остальных выражениях программы.

### 6) `let`

`let` lowering-ится в `CLet`, причём имена специально переименовываются (`_let<id>_<name>`), чтобы избежать конфликтов.

Так обеспечивается корректная локальная область видимости перед следующими шагами (`Flatten`, `CodeGen`).

### 7) `quote`

`quote` обрабатывается отдельно (`lowerQuote`), рекурсивно:

- числа → `make_int`
- символы → `make_symbol`
- списки/cons → `make_cons`
- `nil`/булевы → соответствующие runtime-константы

---

Итог:

- **замыкание** в проекте = `CClosure(function_name, captured_values)`;
- **Lowering** — ключевой этап, где появляется явная модель окружения (`CEnvRef`) и разделение на примитивные runtime-вызовы и вызовы пользовательских closure.
