package lisp.orchestration

class CompilerTest extends munit.FunSuite:

  test("(42) through full pipeline"):
    val cCode = Compiler.pipeline("(42)")
    assert(cCode.contains("make_int(42)"))
    assert(cCode.contains("make_cons"))
    assert(cCode.contains("make_nil"))
