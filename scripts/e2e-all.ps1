foreach ($f in Get-ChildItem examples/**/*.lisp) {
    Write-Host "=== $f ==="
    Get-Content $f
    just run $f 2>$null
    Write-Host
}
