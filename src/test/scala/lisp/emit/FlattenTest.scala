package lisp.emit

import lisp.types.CExpr.*
import lisp.types.Statement.*

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

  test("if expression"):
    val input = CIf(
      CCall("make_bool", List(CNumber(1))),
      CCall("make_int", List(CNumber(1))),
      CCall("make_int", List(CNumber(2)))
    )
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("make_bool", List(CNumber(1)))),
        If("v0",
          List(Value("v1", CCall("make_int", List(CNumber(1)))), Assign("v3", "v1")),
          List(Value("v2", CCall("make_int", List(CNumber(2)))), Assign("v3", "v2")),
          "v3"
        ),
        Return(CVar("v3"))
      )
    )

  test("nested if"):
    val input = CIf(
      CCall("make_bool", List(CNumber(1))),
      CIf(
        CCall("make_bool", List(CNumber(0))),
        CCall("make_int", List(CNumber(1))),
        CCall("make_int", List(CNumber(2)))
      ),
      CCall("make_int", List(CNumber(3)))
    )
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("make_bool", List(CNumber(1)))),
        If("v0",
          List(
            Value("v1", CCall("make_bool", List(CNumber(0)))),
            If("v1",
              List(Value("v2", CCall("make_int", List(CNumber(1)))), Assign("v4", "v2")),
              List(Value("v3", CCall("make_int", List(CNumber(2)))), Assign("v4", "v3")),
              "v4"
            ),
            Assign("v6", "v4")
          ),
          List(Value("v5", CCall("make_int", List(CNumber(3)))), Assign("v6", "v5")),
          "v6"
        ),
        Return(CVar("v6"))
      )
    )
