package lisp.emit

import lisp.types.Line.*

class PrinterTest extends munit.FunSuite:

  test("flat lines"):
    val lines = List(Text("a"), Text("b"))
    assertEquals(Printer(lines), "a\nb")

  test("indented"):
    val lines = List(Text("a"), Text("b"))
    assertEquals(Printer(lines, indent = 1), "  a\n  b")

  test("block"):
    val lines = List(
      Text("if (x) {"),
      Block(List(Text("a;"), Text("b;"))),
      Text("}")
    )
    assertEquals(
      Printer(lines),
      """if (x) {
        |  a;
        |  b;
        |}""".stripMargin
    )

  test("nested blocks"):
    val lines = List(
      Text("if (x) {"),
      Block(
        List(
          Text("if (y) {"),
          Block(List(Text("a;"))),
          Text("}")
        )
      ),
      Text("}")
    )
    assertEquals(
      Printer(lines),
      """if (x) {
        |  if (y) {
        |    a;
        |  }
        |}""".stripMargin
    )
