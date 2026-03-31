package lisp.emit

import lisp.types.CExpr._
import lisp.types.Statement._
import lisp.types.{CExpr, Statement}

import scala.collection.mutable

private class FlattenState:
  private var counter = 0

  def freshVar(): String =
    val name = s"v$counter"
    counter += 1
    name

  def freshEnv(): String =
    val name = s"env_$counter"
    counter += 1
    name

private class FlattenCtx(private val state: FlattenState):
  private val stmts = mutable.ListBuffer[Statement]()

  def emit(stmt: Statement): Unit = stmts += stmt

  def freshVar(): String = state.freshVar()

  def freshEnv(): String = state.freshEnv()

  def nested(): FlattenCtx = FlattenCtx(state)

  def result: List[Statement] = stmts.toList

object Flatten:

  def apply(input: CExpr): List[Statement] =
    val ctx = FlattenCtx(FlattenState())
    ctx.emit(Return(flatten(input, ctx)))
    ctx.result

  def flattenBody(input: CExpr): List[Statement] = apply(input)

  def flattenTopLevelAll(inputs: List[CExpr]): List[Statement] =
    val ctx = FlattenCtx(FlattenState())
    inputs.flatMap(flattenOneTopLevel(_, ctx))

  private def flattenOneTopLevel(input: CExpr, ctx: FlattenCtx): List[Statement] =
    val before = ctx.result.length
    input match
      case CDefineAssign(name, value) =>
        ctx.emit(Define(name, CVar(flattenToVar(value, ctx))))
      case other =>
        ctx.emit(PrintVal(flattenToVar(other, ctx)))
    ctx.result.drop(before)

  private def flattenToVar(input: CExpr, ctx: FlattenCtx): String =
    flatten(input, ctx) match
      case CVar(name) => name
      case leaf =>
        val v = ctx.freshVar()
        ctx.emit(Value(v, leaf))
        v

  def emitExpr(envVars: List[CExpr], ctx: FlattenCtx): String =
    val envName = ctx.freshEnv()
    ctx.emit(EnvDecl(envName, envVars.length))
    val flatEnvVars = envVars.map(flattenToVar(_, ctx))
    flatEnvVars.zipWithIndex.foreach { case (varName, i) => ctx.emit(EnvSet(envName, i, varName)) }
    envName

  private def flatten(input: CExpr, ctx: FlattenCtx): CExpr =
    input match
      case n: CNumber => n
      case v: CVar => v
      case s: CStringLit => s
      case p: CParam => p
      case e: CEnvRef => e
      case CIf(cond, thenBranch, elseBranch) =>
        val condVar = flattenToVar(cond, ctx)
        val thenCtx = ctx.nested()
        val thenVar = flattenToVar(thenBranch, thenCtx)
        val elseCtx = ctx.nested()
        val elseVar = flattenToVar(elseBranch, elseCtx)
        val resultVar = ctx.freshVar()
        ctx.emit(If(condVar, thenCtx.result :+ Assign(resultVar, thenVar), elseCtx.result :+ Assign(resultVar, elseVar), resultVar))
        CVar(resultVar)
      case CCall(name, args) =>
        val newArgs = args.map(flatten(_, ctx))
        val varName = ctx.freshVar()
        ctx.emit(Value(varName, CCall(name, newArgs)))
        CVar(varName)
      case CClosure(funcName, envVars) =>
        if envVars.isEmpty then
          val varName = ctx.freshVar()
          ctx.emit(Value(varName, CCall(Runtime.makeClosure, List(CVar(funcName), CCall(Runtime.makeEnv, List(CNumber(0)))))))
          CVar(varName)
        else
          val envName = emitExpr(envVars, ctx)
          val varName = ctx.freshVar()
          ctx.emit(Value(varName, CCall(Runtime.makeClosure, List(CVar(funcName), CVar(envName)))))
          CVar(varName)
      case CApplyClosure(closure, args) =>
        val closureVar = flattenToVar(closure, ctx)
        val argVars = args.map(flattenToVar(_, ctx))
        val resultVar = ctx.freshVar()
        val argc = CNumber(args.length)
        val argArray = CArgArray(argVars)
        ctx.emit(Value(resultVar, CCall(Runtime.applyClosure, List(CVar(closureVar), argc, argArray))))
        CVar(resultVar)
      case CDefineAssign(name, value) =>
        val valueVar = flattenToVar(value, ctx)
        ctx.emit(Define(name, CVar(valueVar)))
        CVar(name)
      case CLet(bindings, body) =>
        emitExpr(bindings, ctx)
        flatten(body, ctx)
      case CArgArray(_) => input
