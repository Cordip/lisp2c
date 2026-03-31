#!/usr/bin/env bash
passed=0
failed=0

GREEN='\033[32m'
RED='\033[31m'
DIM='\033[2m'
RESET='\033[0m'

run_tests() {
    local dir="$1"

    for f in "$dir"/*.lisp; do
        [ -f "$f" ] || continue
        local expected="${f%.lisp}.expected"
        local name=$(basename "$f" .lisp)
        local expr=$(cat "$f")

        echo "  $name"
        echo -e "${DIM}$(sed 's/^/    /' "$f")${RESET}"

        just build "$f" 2>/dev/null
        if [ $? -ne 0 ]; then
            echo -e "    ${RED}FAIL${RESET} build failed"
            echo ""
            failed=$((failed + 1))
            continue
        fi

        sync
        local actual=$(./output/program)

        if [ -f "$expected" ]; then
            local want=$(cat "$expected")
            if [ "$actual" = "$want" ]; then
                echo -e "    ${GREEN}PASS${RESET} $actual"
            else
                echo -e "    ${RED}FAIL${RESET} got '$actual', expected '$want'"
                failed=$((failed + 1))
                continue
            fi
        else
            echo "    $actual"
        fi

        echo ""
        passed=$((passed + 1))
    done
}

run_error_tests() {
    local dir="$1"

    for f in "$dir"/*.lisp; do
        [ -f "$f" ] || continue
        local expected="${f%.lisp}.expected"
        local name=$(basename "$f" .lisp)

        echo "  $name"
        echo -e "${DIM}$(sed 's/^/    /' "$f")${RESET}"

        local stderr
        stderr=$(just build "$f" 2>&1)
        if [ $? -eq 0 ]; then
            echo -e "    ${RED}FAIL${RESET} expected error but succeeded"
            echo ""
            failed=$((failed + 1))
            continue
        fi

        if [ -f "$expected" ]; then
            local want=$(cat "$expected")
            if echo "$stderr" | grep -q "$want"; then
                echo -e "    ${GREEN}PASS${RESET} error"
            else
                echo -e "    ${RED}FAIL${RESET} wrong error message"
                failed=$((failed + 1))
                continue
            fi
        else
            echo -e "    ${GREEN}PASS${RESET} error"
        fi

        echo ""
        passed=$((passed + 1))
    done
}

echo "=== Basic ==="
echo ""
run_tests "examples/basic"

echo "=== Programs ==="
echo ""
run_tests "examples/programs"

echo "=== Errors ==="
echo ""
run_error_tests "examples/errors"

echo "$passed passed, $failed failed"
[ $failed -eq 0 ] || exit 1
