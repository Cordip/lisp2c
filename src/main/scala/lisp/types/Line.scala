package lisp.types

enum Line:
  case Text(value: String)
  case Block(lines: List[Line])
