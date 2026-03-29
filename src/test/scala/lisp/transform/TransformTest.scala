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

  test("uppercase NIL symbol is nil"):
    assertEquals(Transform(SSymbol("NIL")), LispNil)
    assertEquals(Transform(SSymbol("Nil")), LispNil)
    assertEquals(Transform(SSymbol("nIL")), LispNil)

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

  test("if missing else throws"):
    intercept[Exception] {
      Transform(SList(List(SSymbol("if"), SBool(true), SNumber(1))))
    }

  test("if extra args throws"):
    intercept[Exception] {
      Transform(SList(List(SSymbol("if"), SBool(true), SNumber(1), SNumber(2), SNumber(3))))
    }

  test("if"):
    val input = SList(List(SSymbol("if"), SBool(true), SNumber(1), SNumber(2)))
    assertEquals(
      Transform(input),
      LispIf(LispBool(true), LispNumber(1), LispNumber(2))
    )

  test("nested if"):
    val input = SList(
      List(
        SSymbol("if"),
        SBool(true),
        SList(List(SSymbol("if"), SBool(false), SNumber(1), SNumber(2))),
        SNumber(3)
      )
    )
    assertEquals(
      Transform(input),
      LispIf(
        LispBool(true),
        LispIf(LispBool(false), LispNumber(1), LispNumber(2)),
        LispNumber(3)
      )
    )

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

  test("quote missing body throws"):
    val ex = intercept[Exception] {
      Transform(SList(List(SSymbol("quote"))))
    }
    assertEquals(ex.getMessage, "quote requires exactly 1 argument")

  test("quote extra args throws"):
    val ex = intercept[Exception] {
      Transform(SList(List(SSymbol("quote"), SSymbol("a"), SSymbol("b"))))
    }
    assertEquals(ex.getMessage, "quote requires exactly 1 argument")

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
