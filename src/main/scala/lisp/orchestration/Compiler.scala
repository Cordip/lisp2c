package lisp.orchestration

import lisp.emit.{CodeGen, Flatten, Printer}
import lisp.parse.{Parser, Tokenizer}
import lisp.transform.{Lowering, Transform}

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.Using

object Compiler:

  def pipeline(lispCode: String): String =
    val tokens = Tokenizer(lispCode)
    val exprs = Parser.parseAll(tokens)
    val lispExprs = exprs.map(Transform.apply)
    val cExpr = Lowering(lispExprs.last)
    val statements = Flatten(cExpr)
    val lines = CodeGen(statements)
    Printer(lines, indent = 1)

  private def writeFile(dir: File, name: String, content: String): Unit =
    Using(PrintWriter(File(dir, name)))(_.write(content)).get

  def readResource(name: String): String =
    Using(Source.fromResource(name))(_.mkString).get

  def initializeOutput(output: String): Unit =
    val outDir = File("output")
    if !outDir.exists() then outDir.mkdirs()
    val tagsH = readResource("tags.h")
    val runtimeH = readResource("runtime.h")
    val runtimeC = readResource("runtime.c")
    writeFile(outDir, "tags.h", tagsH)
    writeFile(outDir, "runtime.h", runtimeH)
    writeFile(outDir, "runtime.c", runtimeC)
    writeFile(outDir, "output.c", output)
