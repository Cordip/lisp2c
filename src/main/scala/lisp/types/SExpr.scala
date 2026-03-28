package lisp.types

// тут enum всех возможных типов s-выражений

enum SExpr:
  case SList(value: List[SExpr])
  case SNumber(value: Int)
  case SBool(value: Boolean)
  case SSymbol(value: String)
  case SNil
