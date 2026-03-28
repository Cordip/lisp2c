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
          val condExpr = flatten(cond)
          val condVar = condExpr match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: cond must resolve to a var")

          val startSize = result.size
          val thenExpr = flatten(thenBranch)
          val thenStatements = result.drop(startSize).toList
          result.trimEnd(result.size - startSize)
          val thenVar = thenExpr match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: then must resolve to a var")

          val elseStartSize = result.size
          val elseExpr = flatten(elseBranch)
          val elseStatements = result.drop(elseStartSize).toList
          result.trimEnd(result.size - elseStartSize)
          val elseVar = elseExpr match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: else must resolve to a var")

          val resultVar = s"v$counter"
          counter += 1
          result += If(
            condVar,
            thenStatements :+ Assign(resultVar, thenVar),
            elseStatements :+ Assign(resultVar, elseVar),
            resultVar
          )
          CVar(resultVar)
        case CCall(name, args) =>
          val newArgs = args.map(flatten)
          val varName = s"v$counter"
          counter += 1
          result += Value(varName, CCall(name, newArgs))
          CVar(varName)

    input match
      case _: CCall | _: CIf =>
        val lastVar = flatten(input)
        result += Return(lastVar)
        result.toList
      case _ => throw new Exception("Expected CCall at top level")
