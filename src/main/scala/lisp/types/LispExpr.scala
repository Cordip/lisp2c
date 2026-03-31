package lisp.types

enum LispExpr:
  case LispCons(car: LispExpr, cdr: LispExpr)
  case LispNumber(value: Int)
  case LispBool(value: Boolean)
  case LispSymbol(value: String)
  case LispQuote(body: LispExpr)
  case LispIf(cond: LispExpr, thenBranch: LispExpr, elseBranch: LispExpr)
  case LispApply(function: LispExpr, args: List[LispExpr])
  case LispVar(name: String)
  case LispNil
  case LispLambda(params: List[String], body: LispExpr, freeVars: List[String])
  case LispDefine(name: String, value: LispExpr)
  case LispLet(bindings: List[(String, LispExpr)], body: LispExpr)

  def show: String = this match
    case LispNumber(v)      => v.toString
    case LispSymbol(s)      => s
    case LispBool(v)        => if v then "#t" else "#f"
    case LispNil            => "nil"
    case LispCons(a, b)     => s"(${a.show} . ${b.show})"
    case LispApply(f, args) => s"(${f.show} ${args.map(_.show).mkString(" ")})"
    case LispVar(n)         => n
    case LispQuote(body)    => s"(quote ${body.show})"
    case LispIf(c, t, e)    => s"(if ${c.show} ${t.show} ${e.show})"
    case LispLambda(ps, b, _) => s"(lambda (${ps.mkString(" ")}) ${b.show})"
    case LispDefine(n, v)   => s"(define $n ${v.show})"
    case LispLet(bs, b)     => s"(let (${bs.map((n, v) => s"($n ${v.show})").mkString(" ")}) ${b.show})"
