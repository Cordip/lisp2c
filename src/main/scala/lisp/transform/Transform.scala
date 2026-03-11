package lisp.transform

import lisp.types.SExpr
import lisp.types.SExpr.{SList, SNumber, SNil}
import lisp.types.LispExpr
import lisp.types.LispExpr.{LispCons, LispNumber, LispNil}

object Transform:

    def apply(input: SExpr): LispExpr =
        input match
            case SNil => LispNil
            case SNumber(x) => LispNumber(x)
            case SList(Nil) => LispNil
            case SList(expr :: rest) => LispCons(apply(expr), apply(SList(rest)))
            case _ => throw new Exception("TODO: add more logic")
