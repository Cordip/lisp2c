package lisp.emit

import lisp.types.CExpr.*
import lisp.types.CStatement.*

class FlattenTest extends munit.FunSuite:

  test("simple number call"):
    val input = CCall("make_int", List(CNumber(42)))
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("make_int", List(CNumber(42)))),
        Return(CVar("v0"))
      )
    )

  test("nil call"):
    val input = CCall("make_nil", List())
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("make_nil", List())),
        Return(CVar("v0"))
      )
    )

  test("nested cons"):
    val input = CCall("make_cons", List(
      CCall("make_int", List(CNumber(42))),
      CCall("make_nil", List())
    ))
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("make_int", List(CNumber(42)))),
        Value("v1", CCall("make_nil", List())),
        Value("v2", CCall("make_cons", List(CVar("v0"), CVar("v1")))),
        Return(CVar("v2"))
      )
    )
