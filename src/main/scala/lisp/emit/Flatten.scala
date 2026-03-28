package lisp.emit

import lisp.types.{CExpr, CStatement}
import lisp.types.CExpr.*
import lisp.types.CStatement.*

import scala.collection.mutable

object Flatten:

  def apply(input: CExpr): List[CStatement] =

    val result = mutable.ListBuffer[CStatement]()
    var counter = 0

    def flatten(input: CExpr): CExpr =
      input match
        case n: CNumber => n
        case v: CVar    => v
        case CCall(name, args) =>
          val newArgs = args.map(flatten)
          val varName = s"v$counter"
          counter += 1
          result += Value(varName, CCall(name, newArgs))
          CVar(varName)

    input match
      case _: CCall =>
        val lastVar = flatten(input)
        result += Return(lastVar)
        result.toList
      case _ => throw new Exception("Expected CCall at top level")
