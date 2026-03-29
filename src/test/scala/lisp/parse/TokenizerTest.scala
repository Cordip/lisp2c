package lisp.parse

class TokenizerTest extends munit.FunSuite:

  test("simple number"):
    assertEquals(Tokenizer("(42)"), List("(", "42", ")"))

  test("extra spaces"):
    assertEquals(Tokenizer("(  42  )"), List("(", "42", ")"))

  test("multiple tokens"):
    assertEquals(Tokenizer("(1 2 3)"), List("(", "1", "2", "3", ")"))

  test("symbols"):
    assertEquals(Tokenizer("(+ 1 2)"), List("(", "+", "1", "2", ")"))

  test("booleans"):
    assertEquals(Tokenizer("(if #t #f)"), List("(", "if", "#t", "#f", ")"))

  test("empty string"):
    assertEquals(Tokenizer(""), List())

  test("quote expression"):
    assertEquals(Tokenizer("(quote foo)"), List("(", "quote", "foo", ")"))
