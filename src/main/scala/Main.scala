import lisp.orchestration.Compiler

import java.io.File
import scala.io.Source
import scala.util.Using

@main def lisp2c(args: String*): Unit =
  if args.isEmpty then
    println("Usage: lisp2c <file.lisp | -e expression>")
    sys.exit(1)
  val argList = args.toList
  val lispCode = argList.head match
    case "-e" =>
      if argList.length != 2 then
        println("Usage: lisp2c -e <expression>")
        sys.exit(1)
      argList(1)
    case path =>
      val file = File(path)
      if !file.exists() then
        println(s"File not found: $path")
        sys.exit(1)
      Using(Source.fromFile(file))(_.mkString).get

  val output = Compiler.pipeline(lispCode)
  Compiler.initializeOutput(output)
