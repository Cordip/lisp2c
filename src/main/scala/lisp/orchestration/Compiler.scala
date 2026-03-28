package lisp.orchestration

import lisp.emit.{CodeGen, Flatten}
import lisp.parse.{Parser, Tokenizer}
import lisp.transform.{Lowering, Transform}
import lisp.types.Statement.{If, Value}

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.Using

object Compiler:
  def pipeline(lispCode: String): String =
    val tokens = Tokenizer(lispCode)
    val sExpr = Parser(tokens)
    val lispExpr = Transform(sExpr)
    val cExpr = Lowering(lispExpr)
    val stmts = Flatten(cExpr)
    val lastVar = stmts.last match
      case Value(name, _) => name
      case If(_, _, _, resultVar) => resultVar
      case other => throw new Exception(s"Unexpected last statement type: $other")
    val body = CodeGen(stmts)
    s"$body\nreturn $lastVar;"

  private def writeFile(dir: File, name: String, content: String): Unit =
    Using(PrintWriter(File(dir, name)))(_.write(content))

  def readResource(name: String): String =
    Using(Source.fromResource(name))(_.mkString).get

  def initializeOutput(output: String): Unit =
    val outDir = File("output")
    if (!outDir.exists()) outDir.mkdirs()
    val runtimeH = readResource("runtime.h")
    val runtimeC = readResource("runtime.c")
    writeFile(outDir, "runtime.h", runtimeH)
    writeFile(outDir, "runtime.c", runtimeC)
    writeFile(outDir, "output.c", output)
