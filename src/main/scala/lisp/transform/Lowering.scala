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
      case LispVar(name)                        => throw new Exception(s"unresolved variable: $name")
      case LispQuote(body)                      => lowerQuote(body)
      case LispIf(cond, thenBranch, elseBranch) => CIf(lower(cond), lower(thenBranch), lower(elseBranch))
      case LispApply(LispVar(symbol), args)     => lowerFunc(symbol, args)
      case LispApply(function, _)               => throw new Exception(s"${function.show} is not callable")

  private def lowerQuote(input: LispExpr): CExpr =
    input match
      case LispNil          => CVar(lispNil)
      case LispNumber(n)    => CCall(makeInt, List(CNumber(n)))
      case LispBool(true)   => CVar(lispTrue)
      case LispBool(false)  => CVar(lispFalse)
      case LispCons(a, b)   => CCall(makeCons, List(lowerQuote(a), lowerQuote(b)))
      case LispSymbol(name) => CCall(makeSymbol, List(CStringLit(name)))
      case _                => throw new Exception(s"lowerQuote: unsupported expression: $input")

  private def lowerFunc(symbol: String, args: List[LispExpr]): CExpr =
    variadic.get(symbol) match
      case Some(func) => lowerVariadic(func, symbol, args)
      case None =>
        functions.get(symbol) match
          case Some((func, arity)) if args.length == arity => CCall(func, args.map(lower))
          case Some(_)                                     => throw new Exception(s"wrong arity for $symbol")
          case None => throw new Exception(s"unsupported function $symbol with ${args.length} args")

  private def lowerVariadic(func: String, symbol: String, args: List[LispExpr]): CExpr =
    args match
      case Nil => throw new Exception(s"$symbol requires at least 1 argument")
      case a :: Nil =>
        if symbol == "-" then CCall(lispSub, List(CCall(makeInt, List(CNumber(0))), lower(a)))
        else throw new Exception(s"$symbol requires at least 2 arguments")
      case a :: b :: rest =>
        rest.foldLeft(CCall(func, List(lower(a), lower(b)))) { (acc, arg) =>
          CCall(func, List(acc, lower(arg)))
        }

  private val variadic = Map(
    "+" -> lispAdd,
    "-" -> lispSub,
    "*" -> lispMul
  )

  private val functions = Map(
    "cons" -> (makeCons, 2),
    "=" -> (lispEqv, 2),
    "eq?" -> (lispEq, 2),
    "eqv?" -> (lispEqv, 2),
    "equal?" -> (lispEqual, 2),
    "<" -> (lispLt, 2),
    ">" -> (lispGt, 2),
    "car" -> (lispCar, 1),
    "cdr" -> (lispCdr, 1)
  )
