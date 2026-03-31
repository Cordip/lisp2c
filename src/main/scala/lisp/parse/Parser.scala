package lisp.parse

import lisp.types.SExpr
import lisp.types.SExpr.{SBool, SList, SNumber, SSymbol}

import scala.annotation.tailrec

// TODO: make Either like: Either[String, (SExpr, List[String])]

object Parser:

  def apply(tokens: List[String]): SExpr =
    parse(tokens)._1

  def parseAll(tokens: List[String]): List[SExpr] =
    tokens match
      case Nil => List()
      case _ =>
        val (expr, rest) = parse(tokens)
        expr :: parseAll(rest)

  private def parse(tokens: List[String]): (SExpr, List[String]) =
    tokens match
      case Nil         => throw new Exception("empty input")
      case ")" :: _    => throw new Exception("unexpected )")
      case "(" :: rest => parseList(Nil, rest)
      case x :: rest =>
        x match
          case "#t" => (SBool(true), rest)
          case "#f" => (SBool(false), rest)
          case _ =>
            x.toIntOption match
              case Some(n) => (SNumber(n), rest)
              case None    => (SSymbol(x), rest)

  @tailrec
  private def parseList(acc: List[SExpr], tokens: List[String]): (SExpr, List[String]) =
    tokens match
      case Nil         => throw new Exception("unexpected end of list")
      case ")" :: rest => (SList(acc.reverse), rest)
      case _ =>
        val (expr, remaining) = parse(tokens)
        parseList(expr :: acc, remaining)
