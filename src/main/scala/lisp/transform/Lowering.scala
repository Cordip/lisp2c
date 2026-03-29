package lisp.transform

import lisp.emit.Runtime.*
import lisp.types.{CExpr, LispExpr}
import lisp.types.CExpr.{CCall, CIf, CNumber, CVar}
import lisp.types.LispExpr.*

object Lowering:

  def apply(input: LispExpr): CExpr =
    input match
      case LispNil                              => CVar(lispNil)
      case LispCons(car, cdr)                   => CCall(makeCons, List(apply(car), apply(cdr)))
      case LispNumber(value)                    => CCall(makeInt, List(CNumber(value)))
      case LispBool(true)                       => CVar(lispTrue)
      case LispBool(false)                      => CVar(lispFalse)
      case LispSymbol(value)                    => throw new Exception(s"Unexpected symbol in lowering: $value")
      case LispIf(cond, thenBranch, elseBranch) => CIf(apply(cond), apply(thenBranch), apply(elseBranch))
      case LispApply(LispSymbol(symbol), args)  => lowerFunc(symbol, args)
      case LispApply(function, _)               => throw new Exception(s"${function.show} is not callable")

  private def lowerFunc(symbol: String, args: List[LispExpr]): CExpr =
    functions.get(symbol) match
      case Some((func, arity)) if args.length == arity => CCall(func, args.map(apply))
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
    "car" -> (lispCar, 1),
    "cdr" -> (lispCdr, 1)
  )
