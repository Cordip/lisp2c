package lisp.emit

import lisp.types.CExpr.*
import lisp.types.Statement.*
import lisp.types.{CExpr, Statement}

import scala.collection.mutable

private class FlattenState:
  private var counter = 0

  def freshVar(): String =
    val name = s"v$counter"
    counter += 1
    name

private class FlattenCtx(private val state: FlattenState):
  private val stmts = mutable.ListBuffer[Statement]()

  def emit(stmt: Statement): Unit = stmts += stmt

  def freshVar(): String = state.freshVar()

  def nested(): FlattenCtx = FlattenCtx(state)

  def result: List[Statement] = stmts.toList

object Flatten:

  def apply(input: CExpr): List[Statement] =
    val ctx = FlattenCtx(FlattenState())
    ctx.emit(Return(flatten(input, ctx)))
    ctx.result

  private def flatten(input: CExpr, ctx: FlattenCtx): CExpr =
    input match
      case n: CNumber => n
      case v: CVar    => v
      case s: CStringLit => s
      case CIf(cond, thenBranch, elseBranch) =>
        val condVar = flatten(cond, ctx) match
          case CVar(name) => name
          case _          => throw new Exception("Flatten: cond must resolve to a var")

        val thenCtx = ctx.nested()
        val thenVar = flatten(thenBranch, thenCtx) match
          case CVar(name) => name
          case _          => throw new Exception("Flatten: then must resolve to a var")

        val elseCtx = ctx.nested()
        val elseVar = flatten(elseBranch, elseCtx) match
          case CVar(name) => name
          case _          => throw new Exception("Flatten: else must resolve to a var")

        val resultVar = ctx.freshVar()
        ctx.emit(
          If(
            condVar,
            thenCtx.result :+ Assign(resultVar, thenVar),
            elseCtx.result :+ Assign(resultVar, elseVar),
            resultVar
          )
        )
        CVar(resultVar)
      case CCall(name, args) =>
        val newArgs = args.map(flatten(_, ctx))
        val varName = ctx.freshVar()
        ctx.emit(Value(varName, CCall(name, newArgs)))
        CVar(varName)
