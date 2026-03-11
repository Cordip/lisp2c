package lisp.emit

import lisp.types.CExpr
import lisp.types.CExpr.{CCall, CNumber}

object CodeGen:

    def apply(input: CExpr): String =
        input match
            case CNumber(value) => value.toString()
            case CCall(cmd, expr) => cmd + '(' + expr.map(apply).mkString(", ") + ')'
