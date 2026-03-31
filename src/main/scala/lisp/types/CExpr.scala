package lisp.types

enum CExpr:
  case CCall(name: String, args: List[CExpr])
  case CIf(cond: CExpr, thenBranch: CExpr, elseBranch: CExpr)
  case CNumber(value: Int)
  case CStringLit(value: String)
  case CVar(name: String)
  case CClosure(funcName: String, envVars: List[CExpr])
  case CParam(index: Int)
  case CApplyClosure(closure: CExpr, args: List[CExpr])
  case CDefineAssign(name: String, value: CExpr)
  case CLet(bindings: List[CExpr], body: CExpr)
  case CEnvRef(index: Int)
  case CArgArray(argNames: List[String])
