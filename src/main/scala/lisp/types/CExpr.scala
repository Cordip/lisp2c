package lisp.types

enum CExpr:
  case CCall(name: String, args: List[CExpr])
  case CIf(cond: CExpr, thenBranch: CExpr, elseBranch: CExpr)
  case CNumber(value: Int)
  case CVar(name: String)
