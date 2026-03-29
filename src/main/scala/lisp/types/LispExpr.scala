package lisp.types

enum LispExpr:
  case LispCons(car: LispExpr, cdr: LispExpr)
  case LispNumber(value: Int)
  case LispBool(value: Boolean)
  case LispSymbol(value: String)
  case LispQuote(body: LispExpr)
  case LispIf(cond: LispExpr, thenBranch: LispExpr, elseBranch: LispExpr)
  case LispApply(function: LispExpr, args: List[LispExpr])
  case LispNil

  def show: String = this match
    case LispNumber(v)      => v.toString
    case LispSymbol(s)      => s
    case LispBool(v)        => if v then "#t" else "#f"
    case LispNil            => "nil"
    case LispCons(a, b)     => s"(${a.show} . ${b.show})"
    case LispApply(f, args) => s"(${f.show} ${args.map(_.show).mkString(" ")})"
    case LispQuote(body)    => s"(quote ${body.show})"
    case LispIf(c, t, e)    => s"(if ${c.show} ${t.show} ${e.show})"
