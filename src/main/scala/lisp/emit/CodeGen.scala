package lisp.emit

import lisp.types.*
import lisp.types.CExpr.*
import lisp.types.Line.*
import lisp.types.Statement.*

object CodeGen:

  def apply(input: List[Statement]): List[Line] =
    input.flatMap(emitStatement)

  def renderGlobalDecl(g: GlobalDecl): String =
    s"LispVal ${g.name};"

  def renderFunction(f: FlatFunction): List[String] =
    val sig = s"LispVal ${f.name}(Env* env, int argc, LispVal* argv)"
    val bodyStrings = f.body.flatMap(emitStatement).flatMap(lineToStrings(_, 1))
    List(s"$sig {") ++ bodyStrings ++ List("}")

  private def lineToStrings(line: Line, indent: Int): List[String] =
    line match
      case Text(value) => List(s"${"  " * indent}$value")
      case Block(lines) => lines.flatMap(lineToStrings(_, indent + 1))

  private def emitStatement(stmt: Statement): List[Line] =
    stmt match
      case Value(name, expr) => List(Text(s"LispVal $name = ${emitExpr(expr)};"))
      case Return(expr) => List(Text(s"return ${emitExpr(expr)};"))
      case Assign(target, source) => List(Text(s"$target = $source;"))
      case Define(name, value) => List(Text(s"$name = ${emitExpr(value)};"))
      case EnvDecl(name, size) => List(Text(s"Env* $name = ${Runtime.makeEnv}($size);"))
      case EnvSet(envName, index, valueName) => List(Text(s"$envName->vars[$index] = $valueName;"))
      case PrintVal(varName) => List(Text(s"print_val($varName);"), Text(s"""printf("\\n");"""))
      case If(cond, thenBranch, elseBranch, resultVar) =>
        val thenLines = thenBranch.flatMap(emitStatement)
        val elseLines = elseBranch.flatMap(emitStatement)
        List(Text(s"LispVal $resultVar;"), Text(s"if (is_truthy($cond)) {"), Block(thenLines), Text("} else {"), Block(elseLines), Text("}"))

  private def emitExpr(expr: CExpr): String =
    expr match
      case CNumber(value) => value.toString
      case CStringLit(s) => s"\"$s\""
      case CVar(name) => name
      case CParam(i) => s"argv[$i]"
      case CEnvRef(i) => s"env->vars[$i]"
      case CCall(name, args) => name + "(" + args.map(emitExpr).mkString(", ") + ")"
      case CIf(_, _, _) => throw new Exception("CIf must be flattened before CodeGen")
      case CClosure(_, _) => throw new Exception("CClosure must be flattened before CodeGen")
      case CApplyClosure(_, _) => throw new Exception("CApplyClosure must be flattened before CodeGen")
      case CDefineAssign(_, _) => throw new Exception("CDefineAssign must be flattened before CodeGen")
      case CLet(_, _) => throw new Exception("CLet must be flattened before CodeGen")
      case CArgArray(argNames) =>
        if argNames.isEmpty then "NULL"
        else s"(LispVal[]){${argNames.mkString(", ")}}"
