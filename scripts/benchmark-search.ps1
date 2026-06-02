param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$Keyword = "flu",
  [int]$Iterations = 30
)

$ErrorActionPreference = "Stop"
$headers = @{}
if ($env:AUTH_TOKEN) {
  $headers["Authorization"] = "Bearer $env:AUTH_TOKEN"
}

$durations = @()
for ($i = 0; $i -lt $Iterations; $i++) {
  $sw = [System.Diagnostics.Stopwatch]::StartNew()
  try {
    Invoke-WebRequest -Uri "$BaseUrl/api/doctor/search/keyword?keyword=$Keyword&limit=50" `
      -Headers $headers -UseBasicParsing | Out-Null
  } catch {
    Write-Host "Request failed: $($_.Exception.Message)"
    break
  }
  $sw.Stop()
  $durations += $sw.Elapsed.TotalMilliseconds
}

if ($durations.Count -eq 0) {
  Write-Host "No samples collected."
  exit 1
}

$sorted = $durations | Sort-Object
$avg = ($durations | Measure-Object -Average).Average
$median = $sorted[[int]([math]::Floor($sorted.Count / 2))]
$p95Index = [math]::Min($sorted.Count - 1, [int]([math]::Ceiling($sorted.Count * 0.95)) - 1)
$p95 = $sorted[$p95Index]

Write-Host "Samples: $($durations.Count)"
Write-Host ("Avg(ms): {0:N2}" -f $avg)
Write-Host ("Median(ms): {0:N2}" -f $median)
Write-Host ("P95(ms): {0:N2}" -f $p95)
