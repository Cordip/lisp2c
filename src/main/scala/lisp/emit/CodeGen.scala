package lisp.emit

import lisp.types.{CExpr, CStatement}
import lisp.types.CExpr.{CCall, CNumber, CVar}
import lisp.types.CStatement.*

object CodeGen:

  def apply(input: List[CStatement]): String =
    input.map(emitStatement).mkString("\n")

  private def emitStatement(stmt: CStatement): String =
    stmt match
      case Value(name, call) => s"LispVal* $name = ${emitCall(call)};"
      case Return(expr)      => s"return ${emitExpr(expr)};"

  private def emitCall(call: CCall): String =
    call.name + "(" + call.args.map(emitExpr).mkString(", ") + ")"

  private def emitExpr(expr: CExpr): String =
    expr match
      case CNumber(value)    => value.toString
      case CVar(name)        => name
      case CCall(name, args) => name + "(" + args.map(emitExpr).mkString(", ") + ")"
