package lisp.transform

import lisp.emit.Runtime.*
import lisp.types.CExpr.{CCall, CIf, CNumber}
import lisp.types.LispExpr.*
import lisp.types.{CExpr, LispExpr}

object Lowering:

  def apply(input: LispExpr): CExpr =
    input match
      case LispNil => CCall(makeNil, List())
      case LispCons(car, cdr) => CCall(makeCons, List(apply(car), apply(cdr)))
      case LispNumber(value) => CCall(makeInt, List(CNumber(value)))
      case LispBool(v) => CCall(makeBool, List(CNumber(if v then 1 else 0)))
      case LispSymbol(value) => throw new Exception(s"Unexpected symbol in lowering: $value")
      case LispIf(c, t, e) => CIf(apply(c), apply(t), apply(e))
      case LispQuote(value) => lowerQuote(value)
      case LispApply(LispNumber(value), Nil) => CCall(makeInt, List(CNumber(value)))
      case LispApply(LispSymbol(symbol), args) => lowerFunc(symbol, args)
      case LispApply(function, args) => throw new Exception(s"Unsupported function $function with ${args.length} args")

  private def lowerFunc(symbol: String, args: List[LispExpr]): CExpr =
    functions.get(symbol) match
      case Some((func, arity)) if args.length == arity => CCall(func, args.map(apply))
      case Some(_) => throw new Exception(s"Wrong arity for $symbol")
      case None => throw new Exception(s"Unsupported function $symbol with ${args.length} args")

  private def lowerQuote(value: LispExpr): CExpr =
    value match
      case LispNil => CCall(makeNil, List())
      case LispNumber(n) => CCall(makeInt, List(CNumber(n)))
      case LispBool(v) => CCall(makeBool, List(CNumber(if v then 1 else 0)))
      case LispSymbol(v) => throw new Exception(s"Unsupported quoted symbol $v")
      case LispCons(car, cdr) => CCall(makeCons, List(lowerQuote(car), lowerQuote(cdr)))
      case LispIf(_, _, _) => throw new Exception("Unsupported quoted if")
      case LispQuote(inner) => lowerQuote(inner)
      case LispApply(_, _) => throw new Exception("Unsupported quoted apply")

  private val functions = Map(
    "cons" -> (makeCons, 2),
    "+" -> (lispAdd, 2),
    "-" -> (lispSub, 2),
    "*" -> (lispMul, 2),
  )
