package lisp.emit

import lisp.types.CExpr.*
import lisp.types.Statement.*
import lisp.types.{CExpr, Statement}

object Flatten:

  def apply(input: CExpr): List[Statement] =

    def flatten(input: CExpr, counter: Int): (List[Statement], CExpr, Int) =
      input match
        case n: CNumber => (List(), n, counter)
        case v: CVar => (List(), v, counter)
        case CCall(name, args) =>
          val (argStmtsRev, argExprsRev, nextCounter) =
            args.foldLeft((List.empty[Statement], List.empty[CExpr], counter)) { case ((stmtsAcc, exprsAcc, cnt), arg) =>
              val (argStmts, argExpr, cntNext) = flatten(arg, cnt)
              (argStmts.reverse ::: stmtsAcc, argExpr :: exprsAcc, cntNext)
            }
          val argStmts = argStmtsRev.reverse
          val argExprs = argExprsRev.reverse
          val varName = s"v$nextCounter"
          (argStmts :+ Value(varName, CCall(name, argExprs)), CVar(varName), nextCounter + 1)
        case CIf(cond, thenBranch, elseBranch) =>
          val (condStmts, condExpr, cnt1) = flatten(cond, counter)
          val condVar = condExpr match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: cond must resolve to a var")
          val (thenStmts, thenExpr, cnt2) = flatten(thenBranch, cnt1)
          val thenVar = thenExpr match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: then must resolve to a var")
          val (elseStmts, elseExpr, cnt3) = flatten(elseBranch, cnt2)
          val elseVar = elseExpr match
            case CVar(name) => name
            case _ => throw new Exception("Flatten: else must resolve to a var")
          val resultVar = s"v$cnt3"
          val thenBlock = thenStmts :+ Assign(resultVar, thenVar)
          val elseBlock = elseStmts :+ Assign(resultVar, elseVar)
          val sif = If(condVar, thenBlock, elseBlock, resultVar)
          (condStmts :+ sif, CVar(resultVar), cnt3 + 1)

    input match
      case _: CCall | _: CIf =>
        flatten(input, 0)._1
      case _ => throw new Exception("Expected CCall at top level")
