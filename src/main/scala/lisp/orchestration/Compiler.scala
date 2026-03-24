package lisp.orchestration

import lisp.emit.CodeGen
import lisp.parse.{Parser, Tokenizer}
import lisp.transform.{Lowering, Transform}

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.Using

object Compiler:
  def pipeline(lispCode: String): String =
    val tokens = Tokenizer(lispCode)
    val sExpr = Parser(tokens)
    val lispExpr = Transform(sExpr)
    val cExpr = Lowering(lispExpr)
    CodeGen(cExpr)

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
