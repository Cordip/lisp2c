package lisp.parse

class TokenizerTest extends munit.FunSuite:

  test("simple number"):
    assertEquals(Tokenizer("(42)"), List("(", "42", ")"))

  test("extra spaces"):
    assertEquals(Tokenizer("(  42  )"), List("(", "42", ")"))

  test("multiple tokens"):
    assertEquals(Tokenizer("(1 2 3)"), List("(", "1", "2", "3", ")"))

  test("symbol tokens"):
    assertEquals(Tokenizer("(+ 1 2)"), List("(", "+", "1", "2", ")"))

  test("quote expression"):
    assertEquals(Tokenizer("(quote foo)"), List("(", "quote", "foo", ")"))

  test("bool tokens"):
    assertEquals(Tokenizer("(#t #f)"), List("(", "#t", "#f", ")"))

  test("empty string"):
    assertEquals(Tokenizer(""), List())
