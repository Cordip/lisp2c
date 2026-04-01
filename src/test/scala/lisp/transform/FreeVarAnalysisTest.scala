package lisp.transform

import lisp.types.LispExpr.*

class FreeVarAnalysisTest extends munit.FunSuite:

  test("lambda with no free vars"):
    val input = LispDefine(
      "square",
      LispLambda(
        List("x"),
        LispApply(LispVar("*"), List(LispVar("x"), LispVar("x"))),
        List()
      )
    )
    val result = FreeVarAnalysis(input)
    val LispDefine(_, LispLambda(_, _, freeVars)) = result: @unchecked
    assertEquals(freeVars, List())

  test("var in body is free if not param"):
    val input = LispLambda(
      List("x"),
      LispApply(LispVar("+"), List(LispVar("x"), LispVar("y"))),
      List()
    )
    val result = FreeVarAnalysis(input)
    val LispLambda(_, _, freeVars) = result: @unchecked
    assertEquals(freeVars, List("y"))

  test("nested lambda captures outer parameter"):
    val input = LispDefine(
      "make-adder",
      LispLambda(
        List("n"),
        LispLambda(
          List("x"),
          LispApply(LispVar("+"), List(LispVar("x"), LispVar("n"))),
          List()
        ),
        List()
      )
    )
    val result = FreeVarAnalysis(input)
    val LispDefine(_, LispLambda(_, LispLambda(_, _, freeVars), _)) = result: @unchecked
    assertEquals(freeVars, List("n"))
