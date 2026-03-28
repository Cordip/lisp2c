package lisp.parse

import lisp.types.SExpr.*

class ParserTest extends munit.FunSuite:

  test("single number"):
    val tokens = Tokenizer("(42)")
    assertEquals(Parser(tokens), SList(List(SNumber(42))))

  test("multiple numbers"):
    val tokens = Tokenizer("(1 2 3)")
    assertEquals(Parser(tokens), SList(List(SNumber(1), SNumber(2), SNumber(3))))

  test("nested list"):
    val tokens = Tokenizer("(1 (2 3))")
    assertEquals(
      Parser(tokens),
      SList(List(SNumber(1), SList(List(SNumber(2), SNumber(3)))))
    )

  test("symbol in list"):
    val tokens = Tokenizer("(+ 1 2)")
    assertEquals(Parser(tokens), SList(List(SSymbol("+"), SNumber(1), SNumber(2))))

  test("bool true"):
    val tokens = Tokenizer("(#t)")
    assertEquals(Parser(tokens), SList(List(SBool(true))))

  test("bool false"):
    val tokens = Tokenizer("(if #f 1 2)")
    assertEquals(
      Parser(tokens),
      SList(List(SSymbol("if"), SBool(false), SNumber(1), SNumber(2)))
    )

  test("empty input throws"):
    intercept[Exception] {
      Parser(List())
    }

  test("unexpected close paren throws"):
    intercept[Exception] {
      Parser(List(")"))
    }
