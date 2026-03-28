package lisp.types

import lisp.types.CExpr.*

enum Statement:
  case Return(expr: CExpr)
  case Value(name: String, call: CCall)
  case Assign(target: String, source: String)
  case If(cond: String, thenBranch: List[Statement], elseBranch: List[Statement], resultVar: String)
