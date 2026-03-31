$passed = 0
$failed = 0

function Run-Tests($dir) {
    foreach ($f in Get-ChildItem "$dir/*.lisp" -ErrorAction SilentlyContinue) {
        $expected = $f.FullName -replace '\.lisp$', '.expected'
        $name = $f.BaseName
        $code = Get-Content $f.FullName -Raw

        Write-Host "  $name"
        $code.Split("`n") | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }

        just build $f.FullName 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "    FAIL build failed" -ForegroundColor Red
            Write-Host ""
            $script:failed++
            continue
        }

        $actual = & ./output/program

        if (Test-Path $expected) {
            $want = (Get-Content $expected -Raw).Trim()
            if ($actual.Trim() -eq $want) {
                Write-Host "    PASS $actual" -ForegroundColor Green
            } else {
                Write-Host "    FAIL got '$actual', expected '$want'" -ForegroundColor Red
                $script:failed++
                continue
            }
        } else {
            Write-Host "    $actual"
        }

        Write-Host ""
        $script:passed++
    }
}

function Run-ErrorTests($dir) {
    foreach ($f in Get-ChildItem "$dir/*.lisp" -ErrorAction SilentlyContinue) {
        $expected = $f.FullName -replace '\.lisp$', '.expected'
        $name = $f.BaseName

        Write-Host "  $name"
        (Get-Content $f.FullName -Raw).Split("`n") | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }

        $stderr = just build $f.FullName 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "    FAIL expected error but succeeded" -ForegroundColor Red
            Write-Host ""
            $script:failed++
            continue
        }

        if (Test-Path $expected) {
            $want = (Get-Content $expected -Raw).Trim()
            if ($stderr -match [regex]::Escape($want)) {
                Write-Host "    PASS error" -ForegroundColor Green
            } else {
                Write-Host "    FAIL wrong error message" -ForegroundColor Red
                $script:failed++
                continue
            }
        } else {
            Write-Host "    PASS error" -ForegroundColor Green
        }

        Write-Host ""
        $script:passed++
    }
}

Write-Host "=== Basic ==="
Write-Host ""
Run-Tests "examples/basic"

Write-Host "=== Programs ==="
Write-Host ""
Run-Tests "examples/programs"

Write-Host "=== Errors ==="
Write-Host ""
Run-ErrorTests "examples/errors"

Write-Host "$passed passed, $failed failed"
if ($failed -gt 0) { exit 1 }
