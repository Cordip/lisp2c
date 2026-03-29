package lisp.transform

import lisp.types.{LispExpr, SExpr}
import lisp.types.LispExpr.*
import lisp.types.SExpr.*

object Transform:

  def apply(input: SExpr): LispExpr =
    transform(input)

  private def transform(input: SExpr): LispExpr =
    input match
      case SNil           => LispNil
      case SNumber(x)     => LispNumber(x)
      case SBool(v)       => LispBool(v)
      case SSymbol("nil") => LispNil
      case SSymbol(value) => LispVar(value)
      case SList(Nil)     => LispNil
      case SList(SSymbol("if") :: cond :: thenBranch :: elseBranch :: Nil) =>
        LispIf(transform(cond), transform(thenBranch), transform(elseBranch))
      case SList(SSymbol("if") :: _) => throw new Exception("if requires exactly 3 arguments: condition, then, else")
      case SList(SSymbol("quote") :: body :: Nil) => LispQuote(transformQuoted(body))
      case SList(SSymbol("quote") :: _)           => throw new Exception("quote requires exactly 1 argument")
      case SList(head :: args)                    => LispApply(transform(head), args.map(transform))

  private def transformQuoted(input: SExpr): LispExpr =
    input match
      case SNil             => LispNil
      case SNumber(x)       => LispNumber(x)
      case SBool(value)     => LispBool(value)
      case SSymbol(name)    => LispSymbol(name)
      case SList(Nil)       => LispNil
      case SList(h :: rest) => LispCons(transformQuoted(h), transformQuoted(SList(rest)))
