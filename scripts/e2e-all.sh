#!/usr/bin/env bash
passed=0
failed=0
errors=""

# Success tests: build, run, compare output
for f in examples/basic/*.lisp; do
    expected="${f%.lisp}.expected"
    name=$(basename "$f" .lisp)

    just build "$f" 2>/dev/null
    if [ $? -ne 0 ]; then
        errors="$errors\n  FAIL $name: build failed"
        failed=$((failed + 1))
        continue
    fi

    actual=$(./output/program)

    if [ -f "$expected" ]; then
        want=$(cat "$expected")
        if [ "$actual" = "$want" ]; then
            echo "  PASS $name"
            passed=$((passed + 1))
        else
            echo "  FAIL $name: expected '$want', got '$actual'"
            errors="$errors\n  FAIL $name: expected '$want', got '$actual'"
            failed=$((failed + 1))
        fi
    else
        echo "  SKIP $name (no .expected file)"
    fi
done

# Error tests: expect compilation to fail with specific message
for f in examples/errors/*.lisp; do
    [ -f "$f" ] || continue
    expected="${f%.lisp}.expected"
    name=$(basename "$f" .lisp)

    stderr=$(just build "$f" 2>&1)
    if [ $? -eq 0 ]; then
        echo "  FAIL $name: expected error but build succeeded"
        errors="$errors\n  FAIL $name: expected error but build succeeded"
        failed=$((failed + 1))
        continue
    fi

    if [ -f "$expected" ]; then
        want=$(cat "$expected")
        if echo "$stderr" | grep -q "$want"; then
            echo "  PASS $name (error)"
            passed=$((passed + 1))
        else
            echo "  FAIL $name: expected error '$want'"
            errors="$errors\n  FAIL $name: expected error '$want', got '$stderr'"
            failed=$((failed + 1))
        fi
    else
        echo "  SKIP $name (no .expected file)"
    fi
done

echo ""
echo "$passed passed, $failed failed"
if [ $failed -gt 0 ]; then
    echo -e "\nFailures:$errors"
    exit 1
fi
