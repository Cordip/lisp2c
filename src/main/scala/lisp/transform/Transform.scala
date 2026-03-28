package lisp.transform

import lisp.types.LispExpr.*
import lisp.types.SExpr.{SBool, SList, SNil, SNumber, SSymbol}
import lisp.types.{LispExpr, SExpr}

object Transform:

  def apply(input: SExpr): LispExpr =
    input match
      case SNil => LispNil
      case SNumber(x) => LispNumber(x)
      case SBool(v) => LispBool(v)
      case SSymbol("nil") => LispNil
      case SSymbol(value) => LispSymbol(value)
      case SList(Nil) => LispNil
      case SList(SSymbol("if") :: cond :: thenB :: elseB :: Nil) =>
        LispIf(apply(cond), apply(thenB), apply(elseB))
      case SList(SSymbol("quote") :: quoted :: Nil) =>
        LispQuote(transformQuoted(quoted))
      case SList(head :: args) => LispApply(apply(head), args.map(apply))
      case _ => throw new Exception("TODO: add more logic")

  private def transformQuoted(input: SExpr): LispExpr =
    input match
      case SNil => LispNil
      case SNumber(x) => LispNumber(x)
      case SBool(v) => LispBool(v)
      case SSymbol("nil") => LispNil
      case SSymbol(value) => LispSymbol(value)
      case SList(Nil) => LispNil
      case SList(head :: args) =>
        (head :: args).foldRight(LispNil: LispExpr)((car, cdr) => LispCons(transformQuoted(car), cdr))
