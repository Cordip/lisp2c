#!/usr/bin/env bash
cat <<'EOF'
lisp2c — Lisp-to-C compiler

Usage:
  just build "(42)"                   Lisp → C → GCC (compile only)
  just build examples/basic/num.lisp  Same from file
  just run "(42)"                     Compile and run
  just run examples/basic/num.lisp    Compile and run from file
  just run-all                        Run all examples/
  just test                           Run all tests
  just clean                          Remove build artifacts
  just stop                           Stop sbt server

Examples:
  just run "(42)"                     → prints (42)
  just run "(cons 1 2)"               → prints (1 . 2)
  just run examples/basic/number.lisp → runs file through pipeline
  just run-all                        → runs all examples
EOF
