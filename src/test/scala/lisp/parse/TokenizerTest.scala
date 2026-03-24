package lisp.parse

class TokenizerTest extends munit.FunSuite:

  test("simple number"):
    assertEquals(Tokenizer("(42)"), List("(", "42", ")"))

  test("extra spaces"):
    assertEquals(Tokenizer("(  42  )"), List("(", "42", ")"))

  test("multiple tokens"):
    assertEquals(Tokenizer("(1 2 3)"), List("(", "1", "2", "3", ")"))

  test("empty string"):
    assertEquals(Tokenizer(""), List())
