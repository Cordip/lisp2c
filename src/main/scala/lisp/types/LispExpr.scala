package lisp.types

enum LispExpr:
  case LispCons(car: LispExpr, cdr: LispExpr)
  case LispNumber(value: Int)
  case LispSymbol(value: String)
  case LispApply(function: LispExpr, args: List[LispExpr])
  case LispNil
