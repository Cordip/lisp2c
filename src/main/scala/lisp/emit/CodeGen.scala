package lisp.emit

import lisp.types.{CExpr, Line, Statement}
import lisp.types.CExpr.*
import lisp.types.Line.*
import lisp.types.Statement.*

object CodeGen:

  def apply(input: List[Statement]): List[Line] =
    input.flatMap(emitStatement)

  private def emitStatement(stmt: Statement): List[Line] =
    stmt match
      case Value(name, call)      => List(Text(s"LispVal $name = ${emitCall(call)};"))
      case Return(expr)           => List(Text(s"return ${emitExpr(expr)};"))
      case Assign(target, source) => List(Text(s"$target = $source;"))
      case If(cond, thenBranch, elseBranch, resultVar) =>
        val thenLines = thenBranch.flatMap(emitStatement)
        val elseLines = elseBranch.flatMap(emitStatement)
        List(
          Text(s"LispVal $resultVar;"),
          Text(s"if (is_truthy($cond)) {"),
          Block(thenLines),
          Text("} else {"),
          Block(elseLines),
          Text("}")
        )

  private def emitCall(call: CCall): String =
    call.name + "(" + call.args.map(emitExpr).mkString(", ") + ")"

  private def emitExpr(expr: CExpr): String =
    expr match
      case CNumber(value)    => value.toString
      case CVar(name)        => name
      case CCall(name, args) => name + "(" + args.map(emitExpr).mkString(", ") + ")"
      case CIf(_, _, _)      => throw new Exception("CIf must be flattened before codegen")
