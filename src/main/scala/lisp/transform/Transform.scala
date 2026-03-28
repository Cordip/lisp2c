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
      case SList(SSymbol("if") :: cond :: thenBranch :: elseBranch :: Nil) =>
        LispIf(apply(cond), apply(thenBranch), apply(elseBranch))
      case SList(Nil) => LispNil
      case SList(head :: args) => LispApply(apply(head), args.map(apply))
