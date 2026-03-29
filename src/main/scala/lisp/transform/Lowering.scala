package lisp.transform

import lisp.emit.Runtime.*
import lisp.types.CExpr.*
import lisp.types.LispExpr.*
import lisp.types.{CExpr, LispExpr}

object Lowering:

  def apply(input: LispExpr): CExpr =
    lower(input)

  private def lower(input: LispExpr): CExpr =
    input match
      case LispNil                              => CVar(lispNil)
      case LispCons(car, cdr)                   => CCall(makeCons, List(lower(car), lower(cdr)))
      case LispNumber(value)                    => CCall(makeInt, List(CNumber(value)))
      case LispBool(true)                       => CVar(lispTrue)
      case LispBool(false)                      => CVar(lispFalse)
      case LispSymbol(value)                    => CCall(makeSymbol, List(CStringLit(value)))
      case LispQuote(body)                      => lowerQuote(body)
      case LispIf(cond, thenBranch, elseBranch) => CIf(lower(cond), lower(thenBranch), lower(elseBranch))
      case LispApply(LispSymbol(symbol), args)  => lowerFunc(symbol, args)
      case LispApply(function, _)               => throw new Exception(s"${function.show} is not callable")

  private def lowerQuote(input: LispExpr): CExpr =
    input match
      case LispNil => CVar(lispNil)
      case LispNumber(n) => CCall(makeInt, List(CNumber(n)))
      case LispBool(true) => CVar(lispTrue)
      case LispBool(false) => CVar(lispFalse)
      case LispCons(a, b) => CCall(makeCons, List(lowerQuote(a), lowerQuote(b)))
      case LispSymbol(name) => CCall(makeSymbol, List(CStringLit(name)))
      case _ => throw new Exception(s"lowerQuote: unsupported expression: $input")

  private def lowerFunc(symbol: String, args: List[LispExpr]): CExpr =
    functions.get(symbol) match
      case Some((func, arity)) if args.length == arity => CCall(func, args.map(lower))
      case Some(_)                                     => throw new Exception(s"Wrong arity for $symbol")
      case None => throw new Exception(s"Unsupported function $symbol with ${args.length} args")

  private val functions = Map(
    "cons" -> (makeCons, 2),
    "+" -> (lispAdd, 2),
    "-" -> (lispSub, 2),
    "*" -> (lispMul, 2),
    "=" -> (lispEqv, 2),
    "eq?" -> (lispEq, 2),
    "eqv?" -> (lispEqv, 2),
    "equal?" -> (lispEqual, 2),
    "<" -> (lispLt, 2),
    ">" -> (lispGt, 2),
  )
