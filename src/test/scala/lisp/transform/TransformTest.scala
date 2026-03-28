package lisp.transform

import lisp.types.LispExpr.*
import lisp.types.SExpr.*

class TransformTest extends munit.FunSuite:

  test("single number"):
    val sexpr = SList(List(SNumber(42)))
    assertEquals(Transform(sexpr), LispCons(LispNumber(42), LispNil))

  test("plain number"):
    assertEquals(Transform(SNumber(7)), LispNumber(7))

  test("nil"):
    assertEquals(Transform(SNil), LispNil)

  test("empty list"):
    assertEquals(Transform(SList(Nil)), LispNil)

  test("multiple numbers"):
    val sexpr = SList(List(SNumber(1), SNumber(2), SNumber(3)))
    assertEquals(
      Transform(sexpr),
      LispCons(LispNumber(1), LispCons(LispNumber(2), LispCons(LispNumber(3), LispNil)))
    )

  test("addition apply"):
    val sexpr = SList(List(SSymbol("+"), SNumber(1), SNumber(2)))
    assertEquals(
      Transform(sexpr),
      LispApply(LispSymbol("+"), List(LispNumber(1), LispNumber(2)))
    )
