package lisp.transform

import lisp.types.LispExpr
import lisp.types.LispExpr.*
import lisp.emit.Runtime

object FreeVarAnalysis:

  def apply(expr: LispExpr): LispExpr = analyze(expr, Set())

  private def analyze(expr: LispExpr, bound: Set[String]): LispExpr =
    expr match
      case LispLambda(params, body, _) =>
        val newBound = bound ++ params
        val newBody = analyze(body, newBound)
        val free = collectFree(newBody, params.toSet)
          .filterNot(Runtime.primitives.contains)
          .filterNot(bound.contains)
        LispLambda(params, newBody, free.toList.sorted)
      case LispDefine(name, value) =>
        val newBound = bound + name
        LispDefine(name, analyze(value, newBound))
      case LispApply(fn, args) =>
        LispApply(analyze(fn, bound), args.map(analyze(_, bound)))
      case LispIf(c, t, e) =>
        LispIf(analyze(c, bound), analyze(t, bound), analyze(e, bound))
      case LispLet(bindings, body) =>
        val newBound = bound ++ bindings.map(_._1)
        LispLet(bindings.map((n, v) => (n, analyze(v, bound))), analyze(body, newBound))
      case _ => expr

  private def collectFree(expr: LispExpr, bound: Set[String]): Set[String] =
    expr match
      case LispVar(name) => if bound.contains(name) then Set() else Set(name)
      case LispLambda(params, body, _) =>
        collectFree(body, bound ++ params)
      case LispApply(fn, args) =>
        collectFree(fn, bound) ++ args.flatMap(collectFree(_, bound))
      case LispIf(c, t, e) =>
        collectFree(c, bound) ++ collectFree(t, bound) ++ collectFree(e, bound)
      case LispDefine(name, value) =>
        collectFree(value, bound + name)
      case LispLet(bindings, body) =>
        val bindingFree = bindings.flatMap((_, v) => collectFree(v, bound))
        val newBound = bound ++ bindings.map(_._1)
        bindingFree.toSet ++ collectFree(body, newBound)
      case _ => Set()
