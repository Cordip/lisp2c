package lisp.transform

import lisp.types.LispExpr.*
import lisp.types.SExpr.*
import lisp.types.{LispExpr, SExpr}

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
      case SList(SSymbol("let") :: SList(bindings) :: body :: Nil) =>
        val transformedBindings = bindings.map {
          case SList(SSymbol(name) :: value :: Nil) => (name, transform(value))
          case _                                    => throw new Exception("let binding must be pair (name value)")
        }
        LispLet(transformedBindings, transform(body))
      case SList(SSymbol("let") :: _) => throw new Exception("let requires exactly 2 arguments: bindings and body")
      case SList(SSymbol("define") :: SList(SSymbol(name) :: params) :: body :: Nil) =>
        LispDefine(name, LispLambda(paramNames(params), transform(body), List()))
      case SList(SSymbol("define") :: SSymbol(name) :: value :: Nil) => LispDefine(name, transform(value))
      case SList(SSymbol("lambda") :: SList(params) :: body :: Nil) =>
        LispLambda(paramNames(params), transform(body), List())
      case SList(head :: args) => LispApply(transform(head), args.map(transform))

  private def paramNames(params: List[SExpr]): List[String] =
    params.map { case SSymbol(n) => n; case p => throw new Exception(s"expected param name, got $p") }

  private def transformQuoted(input: SExpr): LispExpr =
    input match
      case SNil             => LispNil
      case SNumber(x)       => LispNumber(x)
      case SBool(value)     => LispBool(value)
      case SSymbol(name)    => LispSymbol(name)
      case SList(Nil)       => LispNil
      case SList(h :: rest) => LispCons(transformQuoted(h), transformQuoted(SList(rest)))
