package lisp.types

enum LispExpr:
  case LispCons(car: LispExpr, cdr: LispExpr)
  case LispNumber(value: Int)
  case LispNil
