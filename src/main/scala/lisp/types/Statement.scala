package lisp.types

import lisp.types.CExpr.*

enum Statement:
  case Return(expr: CExpr)
  case Value(name: String, call: CCall)
