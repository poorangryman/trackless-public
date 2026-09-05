param(
    [string]$NewVersion = ""
)

$versionFile = Join-Path $PSScriptRoot "..\VERSION.txt"
$current = (Get-Content $versionFile).Trim()

if (-not $NewVersion) {
    $parts = $current.Split('.')
    $patch = [int]$parts[2] + 1
    $NewVersion = "$($parts[0]).$($parts[1]).$patch"
}

Write-Host "Bumping TrackLess version: $current -> $NewVersion" -ForegroundColor Cyan

# 1. Update VERSION.txt
Set-Content -Path $versionFile -Value $NewVersion -NoNewline

# 2. Update index.html
$indexFile = Join-Path $PSScriptRoot "..\app\src\main\assets\index.html"
$html = Get-Content $indexFile -Raw
$html = $html -replace "TrackLess v\d+\.\d+\.\d+", "TrackLess v$NewVersion"
Set-Content -Path $indexFile -Value $html -NoNewline

Write-Host "Updated VERSION.txt and index.html to v$NewVersion." -ForegroundColor Green
Write-Host "Run '.\gradlew.bat assembleRelease' to build TrackLess-v$NewVersion.apk" -ForegroundColor Yellow
