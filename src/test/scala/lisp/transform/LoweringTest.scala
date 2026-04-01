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
    val define = LispDefine("f", LispLambda(List("x"), LispVar("x"), List()))
    val applyExpr = LispApply(LispVar("f"), List(LispNumber(42)))
    val (_, _, cExprs) = Lowering.lowerProgram(List(define, applyExpr))
    assert(cExprs(1).isInstanceOf[CApplyClosure])

  test("arity check: too many args to primitive"):
    val ex = intercept[Exception] {
      Lowering(LispApply(LispVar("+"), List(LispNumber(1), LispNumber(2), LispNumber(3))))
    }
    assert(ex.getMessage.contains("requires 2 arguments, got 3"))

  test("arity check: too few args to primitive"):
    val ex = intercept[Exception] {
      Lowering(LispApply(LispVar("+"), List(LispNumber(1))))
    }
    assert(ex.getMessage.contains("requires 2 arguments, got 1"))

  test("arity check: car requires exactly 1 arg"):
    val ex = intercept[Exception] {
      Lowering(LispApply(LispVar("car"), List(LispNil, LispNil)))
    }
    assert(ex.getMessage.contains("requires 1 arguments, got 2"))

  test("let binding resolves to CVar not CEnvRef"):
    val input = LispLet(List("x" -> LispNumber(5)), LispVar("x"))
    val result = Lowering(input)
    result match
      case CLet(namedBindings, body) =>
        assertEquals(namedBindings.length, 1)
        assert(body.isInstanceOf[CVar], s"expected CVar body but got $body")
      case other => fail(s"expected CLet but got $other")

  test("let binding name starts with _let"):
    val input = LispLet(List("x" -> LispNumber(5)), LispVar("x"))
    val result = Lowering(input)
    result match
      case CLet(namedBindings, CVar(bodyName)) =>
        assertEquals(namedBindings.head._1, bodyName)
        assert(bodyName.startsWith("_let"), s"expected _let prefix but got $bodyName")
      case other => fail(s"unexpected result $other")

  test("lowerProgram is stateless across calls"):
    val input = List(LispDefine("id", LispLambda(List("x"), LispVar("x"), List())))
    val (fns1, _, _) = Lowering.lowerProgram(input)
    val (fns2, _, _) = Lowering.lowerProgram(input)
    assertEquals(fns1.head.name, fns2.head.name)

  test("global names are sanitized for C identifiers"):
    val define = LispDefine("make-adder", LispLambda(List("n"), LispVar("n"), List()))
    val applyExpr = LispApply(LispVar("make-adder"), List(LispNumber(5)))
    val (_, globals, cExprs) = Lowering.lowerProgram(List(define, applyExpr))
    assertEquals(globals.head.name, "make_adder")
    cExprs(0) match
      case CDefineAssign(name, _) => assertEquals(name, "make_adder")
      case other                  => fail(s"expected CDefineAssign but got $other")
    cExprs(1) match
      case CApplyClosure(CVar(name), _) => assertEquals(name, "make_adder")
      case other                        => fail(s"expected CApplyClosure(make_adder, ...) but got $other")

  test("sanitized global names remain unique on collision"):
    val defineA = LispDefine("make-adder", LispNumber(1))
    val defineB = LispDefine("make.adder", LispNumber(2))
    val (_, globals, cExprs) = Lowering.lowerProgram(List(defineA, defineB))
    assertEquals(globals.map(_.name), List("make_adder", "make_adder_1"))
    assertEquals(cExprs.collect { case CDefineAssign(name, _) => name }, List("make_adder", "make_adder_1"))
