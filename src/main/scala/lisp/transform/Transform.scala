package lisp.transform

import lisp.types.LispExpr.*
import lisp.types.SExpr.{SList, SNil, SNumber, SSymbol}
import lisp.types.{LispExpr, SExpr}

object Transform:

  def apply(input: SExpr): LispExpr =
    input match
      case SNil => LispNil
      case SNumber(x) => LispNumber(x)
      case SSymbol("nil") => LispNil
      case SSymbol(value) => LispSymbol(value)
      case SList(Nil) => LispNil
      case SList(head :: args) => LispApply(apply(head), args.map(apply))
      case _ => throw new Exception("TODO: add more logic")
