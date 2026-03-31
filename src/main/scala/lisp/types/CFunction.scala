package lisp.types

// Pre-Flatten: body is CExpr. Flatten converts to FlatFunction with List[Statement].
case class CFunction(name: String, params: List[String], body: CExpr)
case class FlatFunction(name: String, params: List[String], body: List[Statement])
case class GlobalDecl(name: String)
