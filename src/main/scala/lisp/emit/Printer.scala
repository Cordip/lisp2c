package lisp.emit

import lisp.types.Line
import lisp.types.Line._

object Printer:

  def apply(lines: List[Line], indent: Int = 0): String =
    lines.flatMap(render(_, indent)).mkString("\n")

  private def render(line: Line, indent: Int): List[String] =
    line match
      case Text(value) => List(s"${"  " * indent}$value")
      case Block(lines) => lines.flatMap(render(_, indent + 1))
