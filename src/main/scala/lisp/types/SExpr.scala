package lisp.types

// тут enum всех возможных типов s-выражений

enum SExpr:
  case SList(value: List[SExpr])
  case SNumber(value: Int)
  case SSymbol(value: String)
  case SNil
