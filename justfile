# lisp2c justfile

sbt := "sbt --client --error"
sbt-verbose := "sbt --client"

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

# Lisp → C → GCC
[group('build')]
build +args:
    {{sbt}} "run {{args}}"
    gcc -Ioutput output/output.c output/runtime.c -lgc -o output/program

# Build and run
[group('run')]
run +args: (build args)
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
test:
    {{sbt-verbose}} test

# Run C runtime tests (Unity)
[group('test')]
test-runtime:
    gcc -Isrc/main/resources -Itest test/test_runtime.c test/unity.c src/main/resources/runtime.c -lgc -o test/test_runtime && ./test/test_runtime

# Format all code
[group('dev')]
fmt:
    scalafmt src/
    clang-format -i src/main/resources/runtime.c src/main/resources/runtime.h

# Check formatting and lint
[group('dev')]
lint:
    scalafmt --check src/
    clang-format --dry-run --Werror src/main/resources/runtime.c src/main/resources/runtime.h
    sbt --no-colors "scalafix --check"

# Auto-fix lint issues + format
[group('dev')]
fix:
    sbt --no-colors "scalafix"
    scalafmt src/
    clang-format -i src/main/resources/runtime.c src/main/resources/runtime.h

# Remove build artifacts
[group('build')]
clean:
    {{sbt}} clean
    rm -rf output/

# Stop sbt server
[group('meta')]
stop:
    sbt --client shutdown
