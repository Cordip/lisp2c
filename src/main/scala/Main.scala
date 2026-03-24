import lisp.orchestration.Compiler

import java.io.File
import scala.io.Source
import scala.util.Using

@main def lisp2c(args: String*): Unit =
  if args.isEmpty then
    println("Usage: lisp2c <file|expression>")
    sys.exit(1)
  val input = args.mkString(" ").trim
  val file = File(input)
  val lispCode =
    if input.startsWith("(") && input.endsWith(")") then
      input
    else
      if !file.exists() then
        println(s"File not found: $input")
        sys.exit(1)
      Using(Source.fromFile(file))(_.mkString).get

  val cCode = Compiler.pipeline(lispCode)
  val template = Compiler.readResource("template.c")
  val output = template.replace("{{EXPR}}", cCode)

  Compiler.initializeOutput(output)
