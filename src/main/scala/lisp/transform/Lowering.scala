package lisp.transform

import lisp.emit.Runtime.*
import lisp.types.CExpr.*
import lisp.types.LispExpr.*
import lisp.types.{CExpr, CFunction, GlobalDecl, LispExpr}

import scala.collection.mutable.ListBuffer

object Lowering:

  case class Scope(
      params: List[String] = List(),
      envVars: List[String] = List(),
      globals: Set[String] = Set(),
      parent: Option[Scope] = None
  ):
    def resolve(name: String): CExpr =
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

  val primitiveMap: Map[String, String] = Map(
    "cons"    -> makeCons,
    "+"       -> lispAdd,
    "-"       -> lispSub,
    "*"       -> lispMul,
    "="       -> lispEqv,
    "eq?"     -> lispEq,
    "eqv?"    -> lispEqv,
    "equal?"  -> lispEqual,
    "<"       -> lispLt,
    ">"       -> lispGt,
    "car"     -> lispCar,
    "cdr"     -> lispCdr
  )

  private var counter = 0
  private val functions = ListBuffer[CFunction]()
  private val globalDecls = ListBuffer[GlobalDecl]()

  // Backward-compat entry point (no scope / no program context)
  def apply(input: LispExpr): CExpr = lowerExpr(input, Scope())

  def lowerProgram(exprs: List[LispExpr]): (List[CFunction], List[GlobalDecl], List[CExpr]) =
    counter = 0
    functions.clear()
    globalDecls.clear()
    val globals = exprs.collect { case LispDefine(name, _) => name }.toSet
    val scope = Scope(globals = globals)
    val cExprs = exprs.flatMap(lowerTopLevel(_, scope))
    (functions.toList, globalDecls.toList, cExprs)

  private def lowerTopLevel(expr: LispExpr, scope: Scope): List[CExpr] =
    expr match
      case LispDefine(name, value) =>
        globalDecls += GlobalDecl(name)
        val cValue = lowerExprWithName(value, name, scope)
        List(CDefineAssign(name, cValue))
      case other =>
        List(lowerExpr(other, scope))

  // Lower with an optional name hint (for naming lambdas derived from define)
  private def lowerExprWithName(expr: LispExpr, hint: String, scope: Scope): CExpr =
    expr match
      case LispLambda(params, body, freeVars) =>
        val funcName = s"lisp_${sanitize(hint)}_$counter"
        counter += 1
        val innerScope = Scope(
          params = params,
          envVars = freeVars,
          globals = scope.globals
        )
        val bodyExpr = lowerExpr(body, innerScope)
        functions += CFunction(funcName, params, bodyExpr)
        val envVarExprs = freeVars.map(n => scope.resolve(n))
        CClosure(funcName, envVarExprs)
      case other => lowerExpr(other, scope)

  def lowerExpr(expr: LispExpr, scope: Scope): CExpr =
    expr match
      case LispNumber(n)  => CCall(makeInt, List(CNumber(n)))
      case LispNil        => CVar(lispNil)
      case LispBool(true) => CVar(lispTrue)
      case LispBool(false) => CVar(lispFalse)
      case LispCons(car, cdr) => CCall(makeCons, List(lowerExpr(car, scope), lowerExpr(cdr, scope)))
      case LispSymbol(name)   => CCall(makeSymbol, List(CStringLit(name)))
      case LispQuote(body)    => lowerQuote(body)
      case LispIf(cond, thenBranch, elseBranch) =>
        CIf(lowerExpr(cond, scope), lowerExpr(thenBranch, scope), lowerExpr(elseBranch, scope))

      case LispVar(name) if primitiveMap.contains(name) =>
        throw new Exception(s"Primitive $name used as value — not supported yet")
      case LispVar(name) => scope.resolve(name)

      case LispLambda(params, body, freeVars) =>
        val funcName = s"lambda_$counter"
        counter += 1
        val innerScope = Scope(
          params = params,
          envVars = freeVars,
          globals = scope.globals
        )
        val bodyExpr = lowerExpr(body, innerScope)
        functions += CFunction(funcName, params, bodyExpr)
        val envVarExprs = freeVars.map(n => scope.resolve(n))
        CClosure(funcName, envVarExprs)

      case LispApply(fn, _) if isLiteral(fn) =>
        throw new Exception(s"${fn.show} is not callable")

      case LispApply(LispVar(name), args) if primitiveMap.contains(name) =>
        CCall(primitiveMap(name), args.map(lowerExpr(_, scope)))

      case LispApply(fn, args) =>
        CApplyClosure(lowerExpr(fn, scope), args.map(lowerExpr(_, scope)))

      case LispLet(bindings, body) =>
        val bindingExprs = bindings.map((_, v) => lowerExpr(v, scope))
        val bindingNames = bindings.map(_._1)
        val innerScope = Scope(
          envVars = bindingNames,
          globals = scope.globals,
          parent = Some(scope)
        )
        val bodyExpr = lowerExpr(body, innerScope)
        CLet(bindingExprs, bodyExpr)

      case _ => throw new Exception(s"Lowering: unsupported: $expr")

  private def isLiteral(expr: LispExpr): Boolean = expr match
    case _: LispNumber | _: LispBool | LispNil | _: LispCons | _: LispSymbol => true
    case _ => false

  private def lowerQuote(input: LispExpr): CExpr =
    input match
      case LispNil          => CVar(lispNil)
      case LispNumber(n)    => CCall(makeInt, List(CNumber(n)))
      case LispBool(true)   => CVar(lispTrue)
      case LispBool(false)  => CVar(lispFalse)
      case LispCons(a, b)   => CCall(makeCons, List(lowerQuote(a), lowerQuote(b)))
      case LispSymbol(name) => CCall(makeSymbol, List(CStringLit(name)))
      case _                => throw new Exception(s"lowerQuote: unsupported expression: $input")

  private def sanitize(name: String): String =
    name.replaceAll("[^a-zA-Z0-9_]", "_")
