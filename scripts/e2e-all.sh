#!/usr/bin/env bash
for f in examples/**/*.lisp; do
    echo "=== $f ==="
    cat "$f"
    just run "$f" 2>/dev/null
    echo
done
