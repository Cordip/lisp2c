package lisp.types

enum SExpr:
  case SList(value: List[SExpr])
  case SNumber(value: Int)
  case SBool(value: Boolean)
  case SSymbol(value: String)
  case SNil
