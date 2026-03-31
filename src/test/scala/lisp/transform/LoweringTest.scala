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
      CVar("LISP_NIL")
    )

  test("quote number"):
    assertEquals(
      Lowering(LispQuote(LispNumber(42))),
      CCall("make_int", List(CNumber(42)))
    )

  test("quote nil"):
    assertEquals(
      Lowering(LispQuote(LispNil)),
      CVar("LISP_NIL")
    )

  test("quote symbol"):
    assertEquals(
      Lowering(LispQuote(LispSymbol("foo"))),
      CCall("make_symbol", List(CStringLit("foo")))
    )

  test("quote cons pair"):
    assertEquals(
      Lowering(LispQuote(LispCons(LispNumber(1), LispNumber(2)))),
      CCall(
        "make_cons",
        List(
          CCall("make_int", List(CNumber(1))),
          CCall("make_int", List(CNumber(2)))
        )
      )
    )

  test("quote list (1 2 3)"):
    val input = LispQuote(LispCons(LispNumber(1), LispCons(LispNumber(2), LispCons(LispNumber(3), LispNil))))
    assertEquals(
      Lowering(input),
      CCall(
        "make_cons",
        List(
          CCall("make_int", List(CNumber(1))),
          CCall(
            "make_cons",
            List(
              CCall("make_int", List(CNumber(2))),
              CCall(
                "make_cons",
                List(
                  CCall("make_int", List(CNumber(3))),
                  CVar("LISP_NIL")
                )
              )
            )
          )
        )
      )
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
      CCall(
        "make_cons",
        List(
          CCall("make_int", List(CNumber(1))),
          CCall(
            "make_cons",
            List(
              CCall("make_int", List(CNumber(2))),
              CCall(
                "make_cons",
                List(
                  CCall("make_int", List(CNumber(3))),
                  CVar("LISP_NIL")
                )
              )
            )
          )
        )
      )
    )

  test("addition (+ 1 2)"):
    assertEquals(
      Lowering(LispApply(LispVar("+"), List(LispNumber(1), LispNumber(2)))),
      CCall("lisp_add", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("nested arithmetic (* 3 (+ 1 2))"):
    assertEquals(
      Lowering(
        LispApply(LispVar("*"), List(LispNumber(3), LispApply(LispVar("+"), List(LispNumber(1), LispNumber(2)))))
      ),
      CCall(
        "lisp_mul",
        List(
          CCall("make_int", List(CNumber(3))),
          CCall("lisp_add", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
        )
      )
    )

  test("subtraction (- 7 4)"):
    assertEquals(
      Lowering(LispApply(LispVar("-"), List(LispNumber(7), LispNumber(4)))),
      CCall("lisp_sub", List(CCall("make_int", List(CNumber(7))), CCall("make_int", List(CNumber(4)))))
    )

  test("number is not callable"):
    intercept[Exception] {
      Lowering(LispApply(LispNumber(42), List()))
    }

  test("bool true"):
    assertEquals(
      Lowering(LispBool(true)),
      CVar("LISP_TRUE")
    )

  test("bool false"):
    assertEquals(
      Lowering(LispBool(false)),
      CVar("LISP_FALSE")
    )

  test("if"):
    assertEquals(
      Lowering(LispIf(LispBool(true), LispNumber(1), LispNumber(2))),
      CIf(
        CVar("LISP_TRUE"),
        CCall("make_int", List(CNumber(1))),
        CCall("make_int", List(CNumber(2)))
      )
    )

  test("= maps to eqv"):
    assertEquals(
      Lowering(LispApply(LispVar("="), List(LispNumber(1), LispNumber(2)))),
      CCall("lisp_eqv", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("eq? maps to eq"):
    assertEquals(
      Lowering(LispApply(LispVar("eq?"), List(LispNumber(1), LispNumber(2)))),
      CCall("lisp_eq", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("equal? maps to equal"):
    assertEquals(
      Lowering(LispApply(LispVar("equal?"), List(LispNumber(1), LispNumber(2)))),
      CCall("lisp_equal", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("< comparison"):
    assertEquals(
      Lowering(LispApply(LispVar("<"), List(LispNumber(1), LispNumber(2)))),
      CCall("lisp_lt", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("> comparison"):
    assertEquals(
      Lowering(LispApply(LispVar(">"), List(LispNumber(1), LispNumber(2)))),
      CCall("lisp_gt", List(CCall("make_int", List(CNumber(1))), CCall("make_int", List(CNumber(2)))))
    )

  test("car"):
    assertEquals(
      Lowering(LispApply(LispVar("car"), List(LispNil))),
      CCall("lisp_car", List(CVar("LISP_NIL")))
    )

  test("cdr"):
    assertEquals(
      Lowering(LispApply(LispVar("cdr"), List(LispNil))),
      CCall("lisp_cdr", List(CVar("LISP_NIL")))
    )

  test("define simple function"):
    val input = LispDefine("id", LispLambda(List("x"), LispVar("x"), List()))
    val (functions, globals, _) = Lowering.lowerProgram(List(input))
    assertEquals(globals.length, 1)
    assertEquals(globals.head.name, "id")
    assertEquals(functions.length, 1)
    assertEquals(functions.head.name, "lisp_id_0")

  test("apply user function"):
    val input = LispApply(LispVar("f"), List(LispNumber(42)))
    val result = Lowering.lowerExpr(input, Lowering.Scope(globals = Set("f")))
    assert(result.isInstanceOf[CApplyClosure])
