package lisp.emit

import lisp.types.Line
import lisp.types.Line.*

object Printer:

  def apply(lines: List[Line], indent: Int = 0): String =
    lines.flatMap(render(_, indent)).mkString("\n")

  private def render(line: Line, indent: Int): List[String] =
    val pad = "  " * indent
    line match
      case Text(value)  => List(s"$pad$value")
      case Block(lines) => lines.flatMap(render(_, indent + 1))
