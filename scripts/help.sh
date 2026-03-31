#!/usr/bin/env bash
cat <<'EOF'
lisp2c — Lisp-to-C compiler

Examples:
  just run-expr '(+ 1 2)'           → 3
  just run-expr "'(1 2 3)"          → (1 2 3)
  just run-expr "(car '(1 2 3))"    → 1
  just run examples/basic/car.lisp  → runs file through pipeline
  just run-all                      → runs all examples

Run 'just' to see all available commands.
EOF
