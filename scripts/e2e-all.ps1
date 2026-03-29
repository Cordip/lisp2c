$passed = 0
$failed = 0
$failures = @()

# Success tests
foreach ($f in Get-ChildItem examples/basic/*.lisp) {
    $expected = $f.FullName -replace '\.lisp$', '.expected'
    $name = $f.BaseName

    just build $f.FullName 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  FAIL $name: build failed"
        $failures += "  FAIL $name: build failed"
        $failed++
        continue
    }

    $actual = & ./output/program

    if (Test-Path $expected) {
        $want = Get-Content $expected -Raw
        if ($actual.Trim() -eq $want.Trim()) {
            Write-Host "  PASS $name"
            $passed++
        } else {
            Write-Host "  FAIL $name: expected '$want', got '$actual'"
            $failures += "  FAIL $name: expected '$want', got '$actual'"
            $failed++
        }
    } else {
        Write-Host "  SKIP $name (no .expected file)"
    }
}

# Error tests
foreach ($f in Get-ChildItem examples/errors/*.lisp -ErrorAction SilentlyContinue) {
    $expected = $f.FullName -replace '\.lisp$', '.expected'
    $name = $f.BaseName

    $stderr = just build $f.FullName 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  FAIL $name: expected error but build succeeded"
        $failures += "  FAIL $name: expected error but build succeeded"
        $failed++
        continue
    }

    if (Test-Path $expected) {
        $want = Get-Content $expected -Raw
        if ($stderr -match [regex]::Escape($want.Trim())) {
            Write-Host "  PASS $name (error)"
            $passed++
        } else {
            Write-Host "  FAIL $name: expected error '$want'"
            $failures += "  FAIL $name: expected error '$want'"
            $failed++
        }
    } else {
        Write-Host "  SKIP $name (no .expected file)"
    }
}

Write-Host ""
Write-Host "$passed passed, $failed failed"
if ($failed -gt 0) {
    Write-Host "`nFailures:"
    $failures | ForEach-Object { Write-Host $_ }
    exit 1
}
