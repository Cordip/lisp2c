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
build input:
    {{sbt}} "run {{input}}"
    gcc -Ioutput output/output.c output/runtime.c -o output/program

# Build and run
[group('run')]
run input: (build input)
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

# Remove build artifacts
[group('build')]
clean:
    {{sbt}} clean
    rm -rf output/

# Stop sbt server
[group('meta')]
stop:
    sbt --client shutdown
