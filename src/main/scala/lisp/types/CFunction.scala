package lisp.types

case class CFunction(name: String, params: List[String], body: CExpr)
case class FlatFunction(name: String, params: List[String], body: List[Statement])
case class GlobalDecl(name: String)
