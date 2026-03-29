package lisp.transform

import lisp.types.SExpr.*
import lisp.types.LispExpr.*

class TransformTest extends munit.FunSuite:

  test("plain number"):
    assertEquals(Transform(SNumber(7)), LispNumber(7))

  test("nil"):
    assertEquals(Transform(SNil), LispNil)

  test("empty list"):
    assertEquals(Transform(SList(Nil)), LispNil)

  test("symbol"):
    assertEquals(Transform(SSymbol("x")), LispSymbol("x"))

  test("quote number"):
    val input = SList(List(SSymbol("quote"), SNumber(42)))
    assertEquals(Transform(input), LispQuote(LispNumber(42)))

  test("quote symbol"):
    val input = SList(List(SSymbol("quote"), SSymbol("foo")))
    assertEquals(Transform(input), LispQuote(LispSymbol("foo")))

  test("quote list"):
    val input = SList(List(SSymbol("quote"), SList(List(SNumber(1), SNumber(2)))))
    assertEquals(
      Transform(input),
      LispQuote(LispCons(LispNumber(1), LispCons(LispNumber(2), LispNil)))
    )

  test("quote empty list"):
    val input = SList(List(SSymbol("quote"), SList(Nil)))
    assertEquals(Transform(input), LispQuote(LispNil))

  test("quote nested quote"):
    val input = SList(List(SSymbol("quote"), SList(List(SSymbol("quote"), SSymbol("foo")))))
    assertEquals(
      Transform(input),
      LispQuote(LispCons(LispSymbol("quote"), LispCons(LispSymbol("foo"), LispNil)))
    )

  test("application"):
    val input = SList(List(SSymbol("+"), SNumber(1), SNumber(2)))
    assertEquals(
      Transform(input),
      LispApply(LispSymbol("+"), List(LispNumber(1), LispNumber(2)))
    )

  test("nested application"):
    val input = SList(List(SSymbol("*"), SNumber(3), SList(List(SSymbol("+"), SNumber(1), SNumber(2)))))
    assertEquals(
      Transform(input),
      LispApply(LispSymbol("*"), List(
        LispNumber(3),
        LispApply(LispSymbol("+"), List(LispNumber(1), LispNumber(2)))
      ))
    )
