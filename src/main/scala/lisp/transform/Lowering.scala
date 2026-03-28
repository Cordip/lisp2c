package lisp.transform

import lisp.emit.Runtime.*
import lisp.types.CExpr.{CCall, CNumber}
import lisp.types.LispExpr.*
import lisp.types.{CExpr, LispExpr}

object Lowering:

  private val runtimeFunctions: Map[String, (String, Int)] = Map(
    "cons" -> (makeCons, 2),
    "+" -> (lispAdd, 2),
    "-" -> (lispSub, 2),
    "*" -> (lispMul, 2)
  )

  private def lowerRuntimeFunction(symbol: String, args: List[LispExpr]): CExpr =
    runtimeFunctions.get(symbol) match
      case Some((runtimeFunction, expectedArity)) if args.length == expectedArity =>
        CCall(runtimeFunction, args.map(apply))
      case Some(_) =>
        throw new Exception(s"Wrong arity for $symbol")
      case None =>
        throw new Exception(s"Unsupported function application: ${LispSymbol(symbol)} with ${args.length} args")

  def apply(input: LispExpr): CExpr =
    input match
      case LispNil => CCall(makeNil, List())
      case LispCons(car, cdr) => CCall(makeCons, List(apply(car), apply(cdr)))
      case LispNumber(value) => CCall(makeInt, List(CNumber(value)))
      case LispSymbol(value) => throw new Exception(s"Unexpected symbol in lowering: $value")
      case LispApply(LispNumber(value), Nil) => CCall(makeInt, List(CNumber(value)))
      case LispApply(LispSymbol(symbol), args) => lowerRuntimeFunction(symbol, args)
      case LispApply(function, args) => throw new Exception(s"Unsupported function application: $function with ${args.length} args")
