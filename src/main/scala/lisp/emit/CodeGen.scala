package lisp.emit

import lisp.types.CExpr.{CCall, CNumber, CVar}
import lisp.types.Statement.*
import lisp.types.{CExpr, Statement}

object CodeGen:

  def apply(input: List[Statement]): String =
    input.flatMap(apply).mkString("\n")

  def apply(stmt: Statement): List[String] =
    stmt match
      case Value(name, call) => List(s"LispVal* $name = ${emitCall(call)};")
      case Return(expr) => List(s"return ${emitExpr(expr)};")
      case Assign(target, source) => List(s"$target = $source;")
      case If(cond, thenBranch, elseBranch, resultVar) =>
        val thenLines = thenBranch.flatMap(apply).map("    " + _)
        val elseLines = elseBranch.flatMap(apply).map("    " + _)
        List(s"LispVal* $resultVar;") ++
          List(s"if (is_truthy($cond)) {") ++
          thenLines ++
          List("} else {") ++
          elseLines ++
          List("}")

  private def emitCall(call: CCall): String =
    call.name + "(" + call.args.map(emitExpr).mkString(", ") + ")"

  private def emitExpr(expr: CExpr): String =
    expr match
      case CNumber(value) => value.toString
      case CVar(name) => name
      case CCall(name, args) => name + "(" + args.map(emitExpr).mkString(", ") + ")"
