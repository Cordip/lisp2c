package lisp.transform

import lisp.types.CExpr.*
import lisp.types.LispExpr.*

class LoweringTest extends munit.FunSuite:

  test("number"):
    assertEquals(
      Lowering(LispNumber(42)),
      CCall("make_int", List(CNumber(42)))
    )

  test("nil"):
    assertEquals(
      Lowering(LispNil),
      CCall("make_nil", List())
    )

  test("cons pair"):
    assertEquals(
      Lowering(LispCons(LispNumber(1), LispNumber(2))),
      CCall("make_cons", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("list (1 2 3)"):
    val input = LispCons(LispNumber(1), LispCons(LispNumber(2), LispCons(LispNumber(3), LispNil)))
    assertEquals(
      Lowering(input),
      CCall("make_cons", List(
        CCall("make_int", List(CNumber(1))),
        CCall("make_cons", List(
          CCall("make_int", List(CNumber(2))),
          CCall("make_cons", List(
            CCall("make_int", List(CNumber(3))),
            CCall("make_nil", List())
          ))
        ))
      ))
    )

  test("addition (+ 1 2)"):
    assertEquals(
      Lowering(LispApply(LispSymbol("+"), List(LispNumber(1), LispNumber(2)))),
      CCall("lisp_add", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("nested arithmetic (* 3 (+ 1 2))"):
    assertEquals(
      Lowering(LispApply(LispSymbol("*"), List(LispNumber(3), LispApply(LispSymbol("+"), List(LispNumber(1), LispNumber(2)))))),
      CCall("lisp_mul", List(
        CCall("make_int", List(CNumber(3))),
        CCall("lisp_add", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
      ))
    )

  test("subtraction (- 7 4)"):
    assertEquals(
      Lowering(LispApply(LispSymbol("-"), List(LispNumber(7), LispNumber(4)))),
      CCall("lisp_sub", List(CCall("make_int", List(CNumber(7))), CCall("make_int", List(CNumber(4)))))
    )

  test("cons application (cons 1 2)"):
    assertEquals(
      Lowering(LispApply(LispSymbol("cons"), List(LispNumber(1), LispNumber(2)))),
      CCall("make_cons", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("wrong arity for mapped function"):
    val exception = intercept[Exception] {
      Lowering(LispApply(LispSymbol("+"), List(LispNumber(1))))
    }
    assertEquals(exception.getMessage, "Wrong arity for +")
