package lisp.emit

import lisp.types.CExpr.*
import lisp.types.Statement.*
import lisp.types.{CExpr, Statement}

import scala.collection.mutable

object Flatten:

  def apply(input: CExpr): List[Statement] =

    val result = mutable.ListBuffer[Statement]()
    var counter = 0

    def flatten(input: CExpr): CExpr =
      input match
        case n: CNumber => n
        case v: CVar => v
        case CIf(cond, thenBranch, elseBranch) =>
          val condVar = flatten(cond) match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: cond must resolve to a var")

          val startSize = result.size
          val thenVar = flatten(thenBranch) match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: then must resolve to a var")

          val thenStatements = result.drop(startSize).toList
          result.dropRightInPlace(result.size - startSize)

          val elseStartSize = result.size
          val elseVar = flatten(elseBranch) match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: else must resolve to a var")

          val elseStatements = result.drop(elseStartSize).toList
          result.dropRightInPlace(result.size - elseStartSize)

          val resultVar = s"v$counter"
          counter += 1
          result += If(condVar, thenStatements :+ Assign(resultVar, thenVar), elseStatements :+ Assign(resultVar, elseVar), resultVar)
          CVar(resultVar)
        case CCall(name, args) =>
          val newArgs = args.map(flatten)
          val varName = s"v$counter"
          counter += 1
          result += Value(varName, CCall(name, newArgs))
          CVar(varName)

    input match
      case _: CCall | _: CIf =>
        result += Return(flatten(input))
        result.toList
      case _ => throw new Exception("Expected CCall at top level")
