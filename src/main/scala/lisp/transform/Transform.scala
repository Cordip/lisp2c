package lisp.transform

import lisp.types.LispExpr.{LispCons, LispNil, LispNumber}
import lisp.types.{LispExpr, SExpr}
import lisp.types.SExpr.{SList, SNil, SNumber}

object Transform:

  def apply(input: SExpr): LispExpr =
    input match
      case SNil => LispNil
      case SNumber(x) => LispNumber(x)
      case SList(Nil) => LispNil
      case SList(expr :: rest) => LispCons(apply(expr), apply(SList(rest)))
      case _ => throw new Exception("TODO: add more logic")
