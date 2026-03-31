# lisp2c justfile

# Show available commands
[group('meta')]
default:
    @just --list

# Show usage examples
[group('meta')]
[unix]
help:
    @./scripts/help.sh

[group('meta')]
[windows]
help:
    @powershell -File scripts/help.ps1

# Lisp → C → GCC (file)
[group('build')]
build file:
    scala-cli run . -- {{file}}
    gcc -Ioutput output/output.c output/runtime.c -o output/program

# Lisp → C → GCC (expression)
[group('build')]
build-expr expr:
    scala-cli run . -- -e "{{expr}}"
    gcc -Ioutput output/output.c output/runtime.c -o output/program

# Build and run (file)
[group('run')]
run file: (build file)
    ./output/program

# Build and run (expression)
[group('run')]
run-expr expr: (build-expr expr)
    ./output/program

# Run all examples
[group('run')]
[unix]
run-all:
    ./scripts/e2e-all.sh

[group('run')]
[windows]
run-all:
    powershell -File scripts/e2e-all.ps1

# Run all tests
[group('test')]
test *args:
    scala-cli test . {{args}}

# Run C runtime tests (Unity)
[group('test')]
test-runtime:
    gcc -Isrc/main/resources -Itest test/test_runtime.c test/unity.c src/main/resources/runtime.c -o test/test_runtime && ./test/test_runtime

# Format all code
[group('dev')]
fmt:
    scalafmt src/
    clang-format -i src/main/resources/*.c src/main/resources/*.h

# Check formatting and lint
[group('dev')]
lint:
    scalafmt --check src/
    clang-format --dry-run --Werror src/main/resources/*.c src/main/resources/*.h
    scala-cli --power fix . --check --enable-built-in=false --scalafix-arg '--files' --scalafix-arg 'src/'

# Auto-fix lint issues + format
[group('dev')]
fix:
    scala-cli --power fix . --enable-built-in=false --scalafix-arg '--files' --scalafix-arg 'src/'
    scalafmt src/
    clang-format -i src/main/resources/*.c src/main/resources/*.h

# Remove build artifacts
[group('build')]
clean:
    rm -rf .scala-build/ output/
