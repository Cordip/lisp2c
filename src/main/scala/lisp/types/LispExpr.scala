package lisp.types

enum LispExpr:
  case LispCons(car: LispExpr, cdr: LispExpr)
  case LispNumber(value: Int)
  case LispBool(value: Boolean)
  case LispSymbol(value: String)
  case LispIf(cond: LispExpr, thenBranch: LispExpr, elseBranch: LispExpr)
  case LispQuote(value: LispExpr)
  case LispApply(function: LispExpr, args: List[LispExpr])
  case LispNil
