package lisp.emit

import lisp.types.CExpr.*
import lisp.types.Statement.*

class FlattenTest extends munit.FunSuite:

  test("single call"):
    val input = CCall("foo", List(CNumber(42)))
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("foo", List(CNumber(42)))),
        Return(CVar("v0"))
      )
    )

  test("call without args"):
    val input = CCall("bar", List())
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("bar", List())),
        Return(CVar("v0"))
      )
    )

  test("nested calls"):
    val input = CCall(
      "outer",
      List(
        CCall("foo", List(CNumber(42))),
        CCall("bar", List())
      )
    )
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("foo", List(CNumber(42)))),
        Value("v1", CCall("bar", List())),
        Value("v2", CCall("outer", List(CVar("v0"), CVar("v1")))),
        Return(CVar("v2"))
      )
    )

  test("if with CVar condition"):
    val input = CIf(
      CVar("cond"),
      CCall("foo", List(CNumber(1))),
      CCall("bar", List(CNumber(2)))
    )
    assertEquals(
      Flatten(input),
      List(
        If(
          "cond",
          List(Value("v0", CCall("foo", List(CNumber(1)))), Assign("v2", "v0")),
          List(Value("v1", CCall("bar", List(CNumber(2)))), Assign("v2", "v1")),
          "v2"
        ),
        Return(CVar("v2"))
      )
    )

  test("if with call condition"):
    val input = CIf(
      CCall("check", List(CNumber(1))),
      CCall("foo", List(CNumber(1))),
      CCall("bar", List(CNumber(2)))
    )
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("check", List(CNumber(1)))),
        If(
          "v0",
          List(Value("v1", CCall("foo", List(CNumber(1)))), Assign("v3", "v1")),
          List(Value("v2", CCall("bar", List(CNumber(2)))), Assign("v3", "v2")),
          "v3"
        ),
        Return(CVar("v3"))
      )
    )

  test("nested if"):
    val input = CIf(
      CCall("check1", List(CNumber(1))),
      CIf(
        CCall("check2", List(CNumber(0))),
        CCall("foo", List(CNumber(1))),
        CCall("bar", List(CNumber(2)))
      ),
      CCall("baz", List(CNumber(3)))
    )
    assertEquals(
      Flatten(input),
      List(
        Value("v0", CCall("check1", List(CNumber(1)))),
        If(
          "v0",
          List(
            Value("v1", CCall("check2", List(CNumber(0)))),
            If(
              "v1",
              List(Value("v2", CCall("foo", List(CNumber(1)))), Assign("v4", "v2")),
              List(Value("v3", CCall("bar", List(CNumber(2)))), Assign("v4", "v3")),
              "v4"
            ),
            Assign("v6", "v4")
          ),
          List(Value("v5", CCall("baz", List(CNumber(3)))), Assign("v6", "v5")),
          "v6"
        ),
        Return(CVar("v6"))
      )
    )
