package lisp.transform

import lisp.emit.Runtime.{makeCons, makeInt, makeNil}
import lisp.types.CExpr.{CCall, CNumber}
import lisp.types.LispExpr.{LispCons, LispNil, LispNumber}
import lisp.types.{CExpr, LispExpr}

object Lowering:

  def apply(input: LispExpr): CExpr =
    input match
      case LispNil => CCall(makeNil, List())
      case LispCons(car, cdr) => CCall(makeCons, List(apply(car), apply(cdr)))
      case LispNumber(value) => CCall(makeInt, List(CNumber(value)))
