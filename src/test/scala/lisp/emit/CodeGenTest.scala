package lisp.emit

import lisp.types.CExpr.*
import lisp.types.Statement.*

class CodeGenTest extends munit.FunSuite:

  test("single value"):
    val input = List(Value("v0", CCall("make_int", List(CNumber(42)))))
    assertEquals(CodeGen(input), "LispVal* v0 = make_int(42);")

  test("multiple values"):
    val input = List(
      Value("v0", CCall("make_int", List(CNumber(42)))),
      Value("v1", CCall("make_nil", List())),
      Value("v2", CCall("make_cons", List(CVar("v0"), CVar("v1"))))
    )
    assertEquals(
      CodeGen(input),
      "LispVal* v0 = make_int(42);\nLispVal* v1 = make_nil();\nLispVal* v2 = make_cons(v0, v1);"
    )

  test("empty args"):
    val input = List(Value("v0", CCall("make_nil", List())))
    assertEquals(CodeGen(input), "LispVal* v0 = make_nil();")

  test("return"):
    val input = List(Value("v0", CCall("make_int", List(CNumber(42)))))
    assertEquals(
      CodeGen(input),
      "LispVal* v0 = make_int(42);"
    )

  test("assign"):
    assertEquals(
      CodeGen.apply(Assign("v3", "v1")),
      List("v3 = v1;")
    )

  test("sif"):
    val input = If("v0",
      List(Value("v1", CCall("make_int", List(CNumber(1)))), Assign("v3", "v1")),
      List(Value("v2", CCall("make_int", List(CNumber(2)))), Assign("v3", "v2")),
      "v3"
    )
    assertEquals(
      CodeGen.apply(input),
      List(
        "LispVal* v3;",
        "if (is_truthy(v0)) {",
        "    LispVal* v1 = make_int(1);",
        "    v3 = v1;",
        "} else {",
        "    LispVal* v2 = make_int(2);",
        "    v3 = v2;",
        "}"
      )
    )
