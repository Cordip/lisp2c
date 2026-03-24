package lisp.emit

import lisp.types.CExpr.*

class CodeGenTest extends munit.FunSuite:

  test("number"):
    assertEquals(CodeGen(CNumber(42)), "42")

  test("simple call"):
    assertEquals(
      CodeGen(CCall("make_int", List(CNumber(42)))),
      "make_int(42)"
    )

  test("nested call"):
    assertEquals(
      CodeGen(CCall("make_cons", List(
        CCall("make_int", List(CNumber(1))),
        CCall("make_nil", List())
      ))),
      "make_cons(make_int(1), make_nil())"
    )

  test("empty args"):
    assertEquals(CodeGen(CCall("make_nil", List())), "make_nil()")
