package lisp.transform

import lisp.emit.Runtime.{lispAdd, lispMul, lispSub, makeCons, makeInt, makeNil}
import lisp.types.CExpr.{CCall, CNumber}
import lisp.types.LispExpr.{LispApply, LispCons, LispNil, LispNumber, LispSymbol}
import lisp.types.{CExpr, LispExpr}

object Lowering:

  def apply(input: LispExpr): CExpr =
    input match
      case LispNil => CCall(makeNil, List())
      case LispCons(car, cdr) => CCall(makeCons, List(apply(car), apply(cdr)))
      case LispNumber(value) => CCall(makeInt, List(CNumber(value)))
      case LispApply(LispSymbol("+"), List(left, right)) => CCall(lispAdd, List(apply(left), apply(right)))
      case LispApply(LispSymbol("-"), List(left, right)) => CCall(lispSub, List(apply(left), apply(right)))
      case LispApply(LispSymbol("*"), List(left, right)) => CCall(lispMul, List(apply(left), apply(right)))
      case LispApply(function, args) =>
        throw new Exception(s"Unsupported function application: $function with ${args.length} args")
      case LispSymbol(value) =>
        throw new Exception(s"Unexpected symbol in lowering: $value")
