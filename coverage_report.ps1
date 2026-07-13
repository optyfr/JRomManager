[xml]$xml = Get-Content "jrmfx\build\reports\jacoco\test\jacocoTestReport.xml"
Write-Host "`n=== JaCoCo Coverage Summary ===`n"

foreach($pkg in $xml.report.package) {
    $instCounter = $pkg.counter | Where-Object { $_.type -eq 'INSTRUCTION' }
    $missed = $instCounter.missed
    $covered = $instCounter.covered
    $total = $missed + $covered
    $pct = if ($total -gt 0) { [math]::Round(($covered / $total) * 100, 1) } else { 0 }
    Write-Host ("{0,-45} {1,6} of {2,6} = {3,5:F1}%" -f $pkg.name, $covered, $total, $pct)
}

Write-Host "`n=== Per-Class Breakdown (jrm.fx.ui package only) ===`n"
$uiPkg = $xml.report.package | Where-Object { $_.name -eq "jrm/fx/ui" }
foreach($cls in $uiPkg.class | Sort-Object name) {
    $instCounter = $cls.counter | Where-Object { $_.type -eq 'INSTRUCTION' }
    $missed = $instCounter.missed
    $covered = $instCounter.covered
    $total = $missed + $covered
    $pct = if ($total -gt 0) { [math]::Round(($covered / $total) * 100, 1) } else { 0 }
    Write-Host ("  {0,-45} {1,5} of {2,5} = {3,5:F1}%" -f $cls.name, $covered, $total, $pct)
}
