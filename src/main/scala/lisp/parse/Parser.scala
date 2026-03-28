package lisp.parse

import lisp.types.SExpr
import lisp.types.SExpr.{SList, SNumber, SSymbol}

import scala.annotation.tailrec

// TODO: make Either like: Either[String, (SExpr, List[String])]

object Parser:
  def apply(tokens: List[String]): SExpr =
    parse(tokens)._1

  private def parse(tokens: List[String]): (SExpr, List[String]) =
    tokens match
      case Nil => throw new Exception("Empty input")
      case ")" :: rest => throw new Exception("Unexpected )")
      case "(" :: rest => parseList(Nil, rest)
      case x :: rest =>
        x.toIntOption match
          case Some(n) => (SNumber(n), rest)
          case None => (SSymbol(x), rest)

  @tailrec
  private def parseList(acc: List[SExpr], tokens: List[String]): (SExpr, List[String]) =
    tokens match
      case Nil => throw new Exception("Unexpected end of list")
      case ")" :: rest => (SList(acc.reverse), rest)
      case _ =>
        val (expr, remaining) = parse(tokens)
        parseList(expr :: acc, remaining)
