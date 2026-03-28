package lisp.types

import lisp.types.CExpr.*

enum CStatement:
  case Return(expr: CExpr)
  case Value(name: String, call: CCall)
