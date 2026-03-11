package lisp.transform

import lisp.types.LispExpr
import lisp.types.LispExpr.{LispNil, LispCons, LispNumber}
import lisp.types.CExpr
import lisp.types.CExpr.{CCall, CNumber}
import lisp.emit.Runtime.{makeCons, makeInt, makeNil}

object Lowering:

    def apply(input: LispExpr): CExpr =
        input match
            case LispNil => CCall(makeNil, List())
            case LispCons(car, cdr) => CCall(makeCons, List(apply(car), apply(cdr)))
            case LispNumber(value) => CCall(makeInt, List(CNumber(value)))
