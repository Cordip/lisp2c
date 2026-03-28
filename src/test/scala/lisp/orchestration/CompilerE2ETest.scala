package lisp.orchestration

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

class CompilerE2ETest extends munit.FunSuite:

  test("e2e (+ 1 2) -> 3"):
    assertEquals(compileAndRun("(+ 1 2)"), "3")

  test("e2e (* 3 (+ 1 2)) -> 9"):
    assertEquals(compileAndRun("(* 3 (+ 1 2))"), "9")

  private def compileAndRun(input: String): String =
    val dir = Files.createTempDirectory("lisp2c-e2e-")
    val runtimeH = dir.resolve("runtime.h")
    val runtimeC = dir.resolve("runtime.c")
    val outputC = dir.resolve("output.c")
    val program = dir.resolve("program")
    try
      val cCode = Compiler.pipeline(input)
      val template = Compiler.readResource("template.c")
      val output = template.replace("{{BODY}}", "    " + cCode.replaceAll("\n", "\n    "))
      Files.writeString(runtimeH, Compiler.readResource("runtime.h"), StandardCharsets.UTF_8)
      Files.writeString(runtimeC, Compiler.readResource("runtime.c"), StandardCharsets.UTF_8)
      Files.writeString(outputC, output, StandardCharsets.UTF_8)

      val compileExit = Process(Seq("gcc", s"-I${dir.toString}", outputC.toString, runtimeC.toString, "-o", program.toString)).!
      assertEquals(compileExit, 0)
      Process(Seq(program.toString)).!!.trim
    finally
      deleteRecursively(dir)

  private def deleteRecursively(path: Path): Unit =
    if Files.exists(path) then
      Files.walk(path).iterator().asScala.toList.reverse.foreach(Files.deleteIfExists)
