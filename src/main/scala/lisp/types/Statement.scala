package lisp.types

enum Statement:
  case Return(expr: CExpr)
  case Value(name: String, expr: CExpr)
  case Assign(target: String, source: String)
  case If(cond: String, thenBranch: List[Statement], elseBranch: List[Statement], resultVar: String)
