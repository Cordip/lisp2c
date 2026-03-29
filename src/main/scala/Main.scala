import lisp.orchestration.Compiler

import java.io.File
import scala.io.Source
import scala.util.Using

@main def lisp2c(args: String*): Unit =
  if args.isEmpty then
    println("Usage: lisp2c <file.lisp | -e expression>")
    sys.exit(1)
  val argList = args.toList
  val lispCode = argList match
    case "-e" :: rest if rest.nonEmpty =>
      rest.mkString(" ").trim
    case _ =>
      val path = argList.mkString(" ").trim
      val file = File(path)
      if !file.exists() then
        println(s"File not found: $path")
        sys.exit(1)
      Using(Source.fromFile(file))(_.mkString).get

  val cCode = Compiler.pipeline(lispCode)
  val template = Compiler.readResource("template.c")
  val output = template.replace("{{BODY}}", cCode)

  Compiler.initializeOutput(output)
