package lisp.parse

// 1) получаем строку
// 2) разбиваем на токены
// 3) возвращаем массив строк

object Tokenizer:

  def apply(input: String, current: String = ""): List[String] =
    val prefix = if current.isEmpty then List() else List(current)
    input.toList match
      case Nil          => prefix
      case '(' :: rest  => prefix ++ List("(") ++ apply(rest.mkString)
      case ')' :: rest  => prefix ++ List(")") ++ apply(rest.mkString)
      case ' ' :: rest  => prefix ++ apply(rest.mkString)
      case '\'' :: rest => prefix ++ List("'") ++ apply(rest.mkString)
      case c :: rest    => apply(rest.mkString, current + c)
