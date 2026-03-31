package lisp.transform

import lisp.emit.Runtime.*
import lisp.types.CExpr.*
import lisp.types.LispExpr.*
import lisp.types.{CExpr, CFunction, GlobalDecl, LispExpr}

import scala.collection.mutable.ListBuffer

object Lowering:

  private case class Scope(
    params: List[String] = List(),
    envVars: List[String] = List(),
    globals: Set[String] = Set(),
    letVars: Map[String, String] = Map(),
    parent: Option[Scope] = None
  ):

    def resolve(name: String): CExpr =
      if letVars.contains(name) then CVar(letVars(name))
      else
        val paramIdx = params.indexOf(name)
        if paramIdx >= 0 then CParam(paramIdx)
        else
          val envIdx = envVars.indexOf(name)
          if envIdx >= 0 then CEnvRef(envIdx)
          else if globals.contains(name) then CVar(name)
          else
            parent match
              case Some(p) => p.resolve(name)
              case None    => throw new Exception(s"unresolved variable: $name")

  private val primitiveMap: Map[String, (String, Int)] = Map(
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

  private class LoweringState:
    private var counter = 0
    val functions: ListBuffer[CFunction] = ListBuffer()
    val globalDecls: ListBuffer[GlobalDecl] = ListBuffer()

    def fresh(): Int =
      val n = counter
      counter += 1
      n

  def apply(input: LispExpr): CExpr =
    lowerExpr(input, Scope(), new LoweringState())

  def lowerProgram(exprs: List[LispExpr]): (List[CFunction], List[GlobalDecl], List[CExpr]) =
    val state = new LoweringState()
    val globals = exprs.collect { case LispDefine(name, _) => name }.toSet
    val scope = Scope(globals = globals)
    val cExprs = exprs.flatMap(lowerTopLevel(_, scope, state))
    (state.functions.toList, state.globalDecls.toList, cExprs)

  private def lowerTopLevel(expr: LispExpr, scope: Scope, state: LoweringState): List[CExpr] =
    expr match
      case LispDefine(name, value) =>
        state.globalDecls += GlobalDecl(name)
        List(CDefineAssign(name, lowerExprWithName(value, name, scope, state)))
      case other =>
        List(lowerExpr(other, scope, state))

  private def lowerExprWithName(expr: LispExpr, hint: String, scope: Scope, state: LoweringState): CExpr =
    expr match
      case LispLambda(params, body, freeVars) =>
        val funcName = s"lisp_${hint.replaceAll("[^a-zA-Z0-9_]", "_")}_${state.fresh()}"
        val innerScope = Scope(params = params, envVars = freeVars, globals = scope.globals)
        val bodyExpr = lowerExpr(body, innerScope, state)
        state.functions += CFunction(funcName, params, bodyExpr)
        CClosure(funcName, freeVars.map(n => scope.resolve(n)))
      case other => lowerExpr(other, scope, state)

  private def lowerExpr(expr: LispExpr, scope: Scope, state: LoweringState): CExpr =
    expr match
      case LispNumber(n)      => CCall(makeInt, List(CNumber(n)))
      case LispNil            => CVar(lispNil)
      case LispBool(true)     => CVar(lispTrue)
      case LispBool(false)    => CVar(lispFalse)
      case LispCons(car, cdr) => CCall(makeCons, List(lowerExpr(car, scope, state), lowerExpr(cdr, scope, state)))
      case LispSymbol(name)   => throw new Exception(s"unresolved variable: $name")
      case LispQuote(body)    => lowerQuote(body)
      case LispIf(cond, thenBranch, elseBranch) =>
        CIf(lowerExpr(cond, scope, state), lowerExpr(thenBranch, scope, state), lowerExpr(elseBranch, scope, state))
      case LispVar(name) if primitiveMap.contains(name) =>
        throw new Exception(s"primitive $name used as value — not supported yet")
      case LispVar(name) => scope.resolve(name)
      case LispLambda(params, body, freeVars) =>
        lowerLambda(s"lambda_${state.fresh()}", params, body, freeVars, scope, state)
      case LispApply(fn, _) if isLiteral(fn) => throw new Exception(s"${fn.show} is not callable")
      case LispApply(LispVar(name), args) if primitiveMap.contains(name) =>
        val (cName, arity) = primitiveMap(name)
        if args.length != arity then
          throw new Exception(s"primitive $name requires $arity arguments, got ${args.length}")
        CCall(cName, args.map(lowerExpr(_, scope, state)))
      case LispApply(fn, args) =>
        CApplyClosure(lowerExpr(fn, scope, state), args.map(lowerExpr(_, scope, state)))
      case LispLet(bindings, body) =>
        val namedBindings = bindings.map { case (name, v) =>
          (s"_let${state.fresh()}_$name", lowerExpr(v, scope, state))
        }
        val letVarMapping = bindings.map(_._1).zip(namedBindings.map(_._1)).toMap
        val innerScope = scope.copy(letVars = scope.letVars ++ letVarMapping)
        CLet(namedBindings, lowerExpr(body, innerScope, state))
      case _ => throw new Exception(s"unsupported: $expr")

  private def lowerLambda(
    funcName: String,
    params: List[String],
    body: LispExpr,
    freeVars: List[String],
    scope: Scope,
    state: LoweringState
  ): CExpr =
    val innerScope = Scope(params = params, envVars = freeVars, globals = scope.globals)
    state.functions += CFunction(funcName, params, lowerExpr(body, innerScope, state))
    CClosure(funcName, freeVars.map(n => scope.resolve(n)))

  private def isLiteral(expr: LispExpr): Boolean = expr match
    case _: LispNumber | _: LispBool | LispNil | _: LispCons | _: LispSymbol => true
    case _                                                                   => false

  private def lowerQuote(input: LispExpr): CExpr =
    input match
      case LispNil          => CVar(lispNil)
      case LispNumber(n)    => CCall(makeInt, List(CNumber(n)))
      case LispBool(true)   => CVar(lispTrue)
      case LispBool(false)  => CVar(lispFalse)
      case LispCons(a, b)   => CCall(makeCons, List(lowerQuote(a), lowerQuote(b)))
      case LispSymbol(name) => CCall(makeSymbol, List(CStringLit(name)))
      case _                => throw new Exception(s"unresolved expression: $input")
