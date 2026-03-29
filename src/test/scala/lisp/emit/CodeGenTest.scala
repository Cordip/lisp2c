package lisp.emit

import lisp.types.CExpr.*
import lisp.types.Line.*
import lisp.types.Statement.*

class CodeGenTest extends munit.FunSuite:

  test("single value"):
    val input = List(Value("v0", CCall("foo", List(CNumber(42)))))
    assertEquals(CodeGen(input), List(Text("LispVal v0 = foo(42);")))

  test("string literal arg"):
    val input = List(Value("v0", CCall("make_symbol", List(CStringLit("foo")))))
    assertEquals(CodeGen(input), List(Text("LispVal v0 = make_symbol(\"foo\");")))

  test("multiple values"):
    val input = List(
      Value("v0", CCall("foo", List(CNumber(42)))),
      Value("v1", CCall("bar", List())),
      Value("v2", CCall("baz", List(CVar("v0"), CVar("v1"))))
    )
    assertEquals(
      CodeGen(input),
      List(
        Text("LispVal v0 = foo(42);"),
        Text("LispVal v1 = bar();"),
        Text("LispVal v2 = baz(v0, v1);")
      )
    )

  test("return"):
    val input = List(
      Value("v0", CCall("foo", List(CNumber(42)))),
      Return(CVar("v0"))
    )
    assertEquals(
      CodeGen(input),
      List(
        Text("LispVal v0 = foo(42);"),
        Text("return v0;")
      )
    )

  test("assign"):
    assertEquals(CodeGen(List(Assign("v3", "v1"))), List(Text("v3 = v1;")))

  test("if statement"):
    val input = List(
      If(
        "v0",
        List(Value("v1", CCall("foo", List(CNumber(1)))), Assign("v3", "v1")),
        List(Value("v2", CCall("bar", List(CNumber(2)))), Assign("v3", "v2")),
        "v3"
      )
    )
    assertEquals(
      CodeGen(input),
      List(
        Text("LispVal v3;"),
        Text("if (is_truthy(v0)) {"),
        Block(List(Text("LispVal v1 = foo(1);"), Text("v3 = v1;"))),
        Text("} else {"),
        Block(List(Text("LispVal v2 = bar(2);"), Text("v3 = v2;"))),
        Text("}")
      )
    )
