package lisp.transform

import lisp.types.LispExpr.*
import lisp.types.SExpr.*

class TransformTest extends munit.FunSuite:

  test("single number"):
    val sexpr = SList(List(SNumber(42)))
    assertEquals(Transform(sexpr), LispApply(LispNumber(42), List()))

  test("plain number"):
    assertEquals(Transform(SNumber(7)), LispNumber(7))

  test("nil"):
    assertEquals(Transform(SNil), LispNil)

  test("empty list"):
    assertEquals(Transform(SList(Nil)), LispNil)

  test("bool"):
    assertEquals(Transform(SBool(true)), LispBool(true))

  test("multiple numbers"):
    val sexpr = SList(List(SNumber(1), SNumber(2), SNumber(3)))
    assertEquals(
      Transform(sexpr),
      LispApply(LispNumber(1), List(LispNumber(2), LispNumber(3)))
    )

  test("addition apply"):
    val sexpr = SList(List(SSymbol("+"), SNumber(1), SNumber(2)))
    assertEquals(
      Transform(sexpr),
      LispApply(LispSymbol("+"), List(LispNumber(1), LispNumber(2)))
    )

  test("user function apply"):
    val sexpr = SList(List(SSymbol("foo"), SNumber(1), SNumber(2)))
    assertEquals(
      Transform(sexpr),
      LispApply(LispSymbol("foo"), List(LispNumber(1), LispNumber(2)))
    )

  test("if"):
    val input = SList(List(SSymbol("if"), SBool(true), SNumber(1), SNumber(2)))
    assertEquals(
      Transform(input),
      LispIf(LispBool(true), LispNumber(1), LispNumber(2))
    )

  test("nested if"):
    val input = SList(List(
      SSymbol("if"), SBool(true),
      SList(List(SSymbol("if"), SBool(false), SNumber(1), SNumber(2))),
      SNumber(3)
    ))
    assertEquals(
      Transform(input),
      LispIf(
        LispBool(true),
        LispIf(LispBool(false), LispNumber(1), LispNumber(2)),
        LispNumber(3)
      )
    )
