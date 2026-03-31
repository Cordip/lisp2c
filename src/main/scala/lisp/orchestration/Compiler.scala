package lisp.orchestration

import lisp.emit.{CodeGen, Flatten, Printer}
import lisp.parse.{Parser, Tokenizer}
import lisp.transform.{FreeVarAnalysis, Lowering, Transform}
import lisp.types.{FlatFunction, GlobalDecl}

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.Using

object Compiler:

  def pipeline(lispCode: String): String =
    val tokens = Tokenizer(lispCode)
    val sexprs = Parser.parseAll(tokens)
    val lispExprs = sexprs.map(Transform.apply)
    val analyzed = lispExprs.map(FreeVarAnalysis.apply)
    val (cfunctions, globalDecls, cExprs) = Lowering.lowerProgram(analyzed)
    val flatFunctions = cfunctions.map(f => FlatFunction(f.name, f.params, Flatten.flattenBody(f.body)))
    val mainStmts = Flatten.flattenTopLevelAll(cExprs)
    val globalLines = globalDecls.map(CodeGen.renderGlobalDecl)
    val functionLines = flatFunctions.flatMap(CodeGen.renderFunction)
    val bodyLines = CodeGen(mainStmts).flatMap(line => Printer(List(line), indent = 1).split("\n").toList)
    val template = readResource("template.c")
    template
      .replace("{{GLOBALS}}", globalLines.mkString("\n"))
      .replace("{{FUNCTIONS}}", functionLines.mkString("\n"))
      .replace("{{BODY}}", bodyLines.mkString("\n"))

  private def writeFile(dir: File, name: String, content: String): Unit =
    Using(PrintWriter(File(dir, name)))(_.write(content)).get

  private def readResource(name: String): String =
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
